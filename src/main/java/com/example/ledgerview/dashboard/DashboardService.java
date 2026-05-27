package com.example.ledgerview.dashboard;

import com.example.ledgerview.account.AccountRepository;
import com.example.ledgerview.category.Category;
import com.example.ledgerview.category.CategoryType;
import com.example.ledgerview.category.CategoryRepository;
import com.example.ledgerview.transaction.Transaction;
import com.example.ledgerview.transaction.TransactionRepository;
import com.example.ledgerview.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(AccountRepository accountRepository,
                            CategoryRepository categoryRepository,
                            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardController.SummaryResponse summary(UUID userId) {
        List<DashboardController.CurrencyAmount> totalBalance = accountRepository
                .sumBalanceByCurrencyAndUserId(userId)
                .stream()
                .map(row -> new DashboardController.CurrencyAmount((String) row[0], (BigDecimal) row[1]))
                .sorted(Comparator.comparing(DashboardController.CurrencyAmount::currency))
                .toList();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant from = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        Map<UUID, Category> categoryMap = categoryRepository.findAllByUserIdOrderByName(userId)
                .stream().collect(Collectors.toMap(Category::getId, c -> c));

        Map<String, BigDecimal> incomeByCurrency = transactionRepository
                .findByUserIdAndTypeAndDateBetween(userId, TransactionType.INCOME, from, to)
                .stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)
                ));

        Map<String, BigDecimal> expenseByCurrency = transactionRepository
                .findByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, from, to)
                .stream()
                .filter(t -> {
                    Category c = categoryMap.get(t.getCategoryId());
                    return c == null || c.getType() != CategoryType.TRANSFER;
                })
                .collect(Collectors.groupingBy(
                        Transaction::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)
                ));

        Set<String> allCurrencies = new TreeSet<>();
        allCurrencies.addAll(incomeByCurrency.keySet());
        allCurrencies.addAll(expenseByCurrency.keySet());

        List<DashboardController.CurrencyAmount> monthlyIncome = incomeByCurrency.entrySet().stream()
                .map(e -> new DashboardController.CurrencyAmount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardController.CurrencyAmount::currency))
                .toList();

        List<DashboardController.CurrencyAmount> monthlyExpenses = expenseByCurrency.entrySet().stream()
                .map(e -> new DashboardController.CurrencyAmount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardController.CurrencyAmount::currency))
                .toList();

        List<DashboardController.CurrencyAmount> netFlow = allCurrencies.stream()
                .map(currency -> new DashboardController.CurrencyAmount(
                        currency,
                        incomeByCurrency.getOrDefault(currency, BigDecimal.ZERO)
                                .subtract(expenseByCurrency.getOrDefault(currency, BigDecimal.ZERO))
                ))
                .toList();

        return new DashboardController.SummaryResponse(totalBalance, monthlyIncome, monthlyExpenses, netFlow);
    }

    @Transactional(readOnly = true)
    public List<DashboardController.ExpenseByCategoryResponse> expensesByCategory(UUID userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        Instant from = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = start.withDayOfMonth(start.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        Map<UUID, Category> categoryMap = categoryRepository.findAllByUserIdOrderByName(userId)
                .stream().collect(Collectors.toMap(Category::getId, c -> c));

        record Key(UUID categoryId, String currency) {}

        Map<Key, BigDecimal> totals = transactionRepository
                .findByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, from, to)
                .stream()
                .filter(t -> {
                    Category c = categoryMap.get(t.getCategoryId());
                    return c != null && c.getType() != CategoryType.TRANSFER;
                })
                .collect(Collectors.groupingBy(
                        t -> new Key(t.getCategoryId(), t.getCurrency()),
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)
                ));

        return totals.entrySet().stream()
                .map(e -> {
                    Category c = categoryMap.get(e.getKey().categoryId());
                    return new DashboardController.ExpenseByCategoryResponse(
                            e.getKey().categoryId(), c.getName(), c.getColor(),
                            e.getKey().currency(), e.getValue());
                })
                .sorted(Comparator.comparing(DashboardController.ExpenseByCategoryResponse::total).reversed())
                .toList();
    }
}

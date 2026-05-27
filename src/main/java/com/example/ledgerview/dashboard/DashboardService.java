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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        BigDecimal totalBalance = accountRepository.sumBalanceByUserId(userId);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant firstOfMonth = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant lastOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        BigDecimal monthlyIncome = transactionRepository.sumByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, firstOfMonth, lastOfMonth);

        Map<UUID, Category> categoryMap = categoryRepository.findAllByUserIdOrderByName(userId)
                .stream().collect(Collectors.toMap(Category::getId, c -> c));

        List<Transaction> expenseTransactions = transactionRepository.findByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, firstOfMonth, lastOfMonth);

        BigDecimal monthlyExpenses = expenseTransactions.stream()
                .filter(t -> {
                    Category c = categoryMap.get(t.getCategoryId());
                    return c == null || c.getType() != CategoryType.TRANSFER;
                })
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netFlow = monthlyIncome.subtract(monthlyExpenses);

        return new DashboardController.SummaryResponse(totalBalance, monthlyIncome, monthlyExpenses, netFlow);
    }

    @Transactional(readOnly = true)
    public List<DashboardController.ExpenseByCategoryResponse> expensesByCategory(UUID userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        Instant from = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = start.withDayOfMonth(start.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        Map<UUID, Category> categoryMap = categoryRepository.findAllByUserIdOrderByName(userId)
                .stream().collect(Collectors.toMap(Category::getId, c -> c));

        Map<UUID, BigDecimal> totals = transactionRepository
                .findByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, from, to)
                .stream()
                .filter(t -> {
                    Category c = categoryMap.get(t.getCategoryId());
                    return c != null && c.getType() != CategoryType.TRANSFER;
                })
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)
                ));

        return totals.entrySet().stream()
                .map(e -> {
                    Category c = categoryMap.get(e.getKey());
                    return new DashboardController.ExpenseByCategoryResponse(
                            e.getKey(), c.getName(), c.getColor(), e.getValue());
                })
                .sorted(Comparator.comparing(DashboardController.ExpenseByCategoryResponse::total).reversed())
                .toList();
    }
}

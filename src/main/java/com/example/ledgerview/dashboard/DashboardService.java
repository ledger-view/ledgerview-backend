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
import java.time.temporal.WeekFields;
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

    @Transactional(readOnly = true)
    public List<DashboardController.CashflowRow> cashflow(UUID userId, int weeks, UUID accountId) {
        int capped = Math.min(Math.max(weeks, 1), 52);
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);

        List<WeekWindow> windows = buildWeekWindows(today, capped);
        Instant from = windows.get(0).start();

        List<Transaction> txs = accountId != null
                ? transactionRepository.findByUserIdAndAccountIdAndDateBetween(userId, accountId, from, now)
                : transactionRepository.findByUserIdAndDateBetween(userId, from, now);

        record Key(String weekLabel, Instant weekStart, String currency, UUID accId) {}

        Map<Key, BigDecimal[]> buckets = new LinkedHashMap<>();
        for (Transaction t : txs) {
            WeekWindow w = findWindow(windows, t.getDate());
            if (w == null) continue;
            Key k = new Key(w.label(), w.start(), t.getCurrency(), t.getAccountId());
            BigDecimal[] sums = buckets.computeIfAbsent(k, x -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                sums[0] = sums[0].add(t.getAmount());
            } else {
                sums[1] = sums[1].add(t.getAmount().abs());
            }
        }

        return buckets.entrySet().stream()
                .map(e -> new DashboardController.CashflowRow(
                        e.getKey().weekLabel(), e.getKey().weekStart(),
                        e.getKey().currency(), e.getKey().accId(),
                        e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    private record WeekWindow(String label, Instant start, Instant end) {}

    private List<WeekWindow> buildWeekWindows(LocalDate today, int count) {
        List<WeekWindow> result = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            LocalDate endDate = today.minusDays((long) i * 7);
            LocalDate startDate = endDate.minusDays(6);
            Instant startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toInstant();
            int weekNum = startDate.get(WeekFields.ISO.weekOfWeekBasedYear());
            result.add(new WeekWindow("W" + weekNum, startInstant, endInstant));
        }
        return result;
    }

    private WeekWindow findWindow(List<WeekWindow> windows, Instant date) {
        for (WeekWindow w : windows) {
            if (!date.isBefore(w.start()) && !date.isAfter(w.end())) return w;
        }
        return null;
    }
}

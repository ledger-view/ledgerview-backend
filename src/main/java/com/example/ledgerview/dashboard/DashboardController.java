package com.example.ledgerview.dashboard;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.util.ApiPaths;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.DASHBOARD)
public class DashboardController {

    record CurrencyAmount(String currency, BigDecimal amount) {}

    record SummaryResponse(
            List<CurrencyAmount> totalBalance,
            List<CurrencyAmount> monthlyIncome,
            List<CurrencyAmount> monthlyExpenses,
            List<CurrencyAmount> netFlow
    ) {}

    record ExpenseByCategoryResponse(
            UUID categoryId,
            String categoryName,
            String categoryColor,
            String currency,
            BigDecimal total
    ) {}

    record CashflowRow(
            String weekLabel,
            Instant weekStart,
            String currency,
            UUID accountId,
            BigDecimal income,
            BigDecimal expense
    ) {}

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.summary(user.id());
    }

    @GetMapping("/cashflow")
    public List<CashflowRow> cashflow(
            @RequestParam(defaultValue = "12") int weeks,
            @RequestParam(required = false) UUID accountId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return service.cashflow(user.id(), weeks, accountId);
    }

    @GetMapping("/expenses-by-category")
    public List<ExpenseByCategoryResponse> expensesByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return service.expensesByCategory(user.id(), y, m);
    }
}

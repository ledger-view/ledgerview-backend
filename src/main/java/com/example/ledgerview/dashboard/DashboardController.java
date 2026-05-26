package com.example.ledgerview.dashboard;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.util.ApiPaths;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.DASHBOARD)
public class DashboardController {

    record SummaryResponse(
            BigDecimal totalBalance,
            BigDecimal monthlyIncome,
            BigDecimal monthlyExpenses,
            BigDecimal netFlow
    ) {}

    record ExpenseByCategoryResponse(
            UUID categoryId,
            String categoryName,
            String categoryColor,
            BigDecimal total
    ) {}

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.summary(user.id());
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

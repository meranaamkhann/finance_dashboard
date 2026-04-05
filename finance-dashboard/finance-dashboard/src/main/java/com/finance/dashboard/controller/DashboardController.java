package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.DashboardService;
import com.finance.dashboard.service.FinancialHealthScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "6. Dashboard & Analytics", description = "Aggregated insights — cached for performance")
public class DashboardController {

    private final DashboardService           dashboardService;
    private final FinancialHealthScoreService healthScoreService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Full dashboard summary [ALL ROLES]",
               description = "Returns income/expenses/net, category map, recent transactions, " +
                             "budget statuses, 6-month trend, and financial health score. Cached.")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummary()));
    }

    @GetMapping("/summary/range")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Summary for a custom date range [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getSummaryByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to))
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("'from' must not be after 'to'"));
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummaryByRange(from, to)));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Category-wise totals with % share [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getCategorySummary()));
    }

    @GetMapping("/trends/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Monthly income vs expense trend [ADMIN, ANALYST]",
               description = "Returns up to 24 months of data. Each entry includes `savingsRate`.")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {
        if (months < 1 || months > 24)
            return ResponseEntity.badRequest().body(ApiResponse.fail("months must be 1–24"));
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getMonthlyTrends(months)));
    }

    @GetMapping("/trends/weekly")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Weekly income vs expense trend [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<List<WeeklyTrendResponse>>> getWeeklyTrends(
            @RequestParam(defaultValue = "8") int weeks) {
        if (weeks < 1 || weeks > 52)
            return ResponseEntity.badRequest().body(ApiResponse.fail("weeks must be 1–52"));
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getWeeklyTrends(weeks)));
    }

    @GetMapping("/health-score")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Financial health score (0–100) with grade and insights [ADMIN, ANALYST]",
               description = "Computed from 5 signals: savings rate, budget adherence, " +
                             "expense diversity, income stability, positive cash-flow months.")
    public ResponseEntity<ApiResponse<FinancialHealthScoreResponse>> getHealthScore() {
        return ResponseEntity.ok(ApiResponse.ok(healthScoreService.compute()));
    }

    @GetMapping("/top-expenses")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Top N expense categories this month [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTopExpenses(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getTopExpenseCategories(limit)));
    }

    @GetMapping("/spending-by-day")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Spending by day-of-week pattern [ADMIN, ANALYST]",
               description = "Shows which days of the week you spend the most. " +
                             "0 = Sunday, 1 = Monday … 6 = Saturday.")
    public ResponseEntity<ApiResponse<List<Object[]>>> getSpendingByDay() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSpendingByDayOfWeek()));
    }
}

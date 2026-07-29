package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.service.DashboardService;
import com.finance.dashboard.service.FinancialHealthScoreService;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    private final DashboardService dashboardService;
    private final FinancialHealthScoreService healthScoreService;
    private final SecurityUtils securityUtils;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummary()));
    }

    @GetMapping("/summary/range")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummaryRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to)) throw new BadRequestException("'from' must be before 'to'");
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummaryForRange(from, to)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategories(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate f = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate t = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getCategoryBreakdown(f, t)));
    }

    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getMonthlyTrend(Math.max(1, Math.min(months, 24)))));
    }

    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyTrendResponse>>> getWeeklyTrend(
            @RequestParam(defaultValue = "12") int weeks) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getWeeklyTrend(Math.max(1, Math.min(weeks, 52)))));
    }

    @GetMapping("/top-expenses")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getTopExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {
        LocalDate f = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate t = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getTopExpenses(f, t, Math.max(1, Math.min(limit, 20)))));
    }

    @GetMapping("/spending-by-day")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getSpendingByDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate f = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate t = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSpendingByDayOfWeek(f, t)));
    }

    @GetMapping("/health-score")
    public ResponseEntity<ApiResponse<FinancialHealthScoreResponse>> getHealthScore() {
        return ResponseEntity.ok(ApiResponse.ok(healthScoreService.calculate(securityUtils.getCurrentUserId())));
    }
}

package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.service.*;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
@Tag(name="Dashboard") @SecurityRequirement(name="bearerAuth")
public class DashboardController {
    private final DashboardService dashboardService;
    private final FinancialHealthScoreService healthScoreService;
    private final SecurityUtils securityUtils;

    @GetMapping("/summary") @Operation(summary="Current month summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() { return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummary())); }
    @GetMapping("/summary/range") @Operation(summary="Custom range summary — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummaryRange(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        if(from.isAfter(to)) throw new BadRequestException("'from' must be before 'to'");
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummaryForRange(from,to)));
    }
    @GetMapping("/categories") @Operation(summary="Expense by category — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategories(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate f=from!=null?from:LocalDate.now().withDayOfMonth(1),t=to!=null?to:LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getCategoryBreakdown(f,t)));
    }
    @GetMapping("/trends/monthly") @Operation(summary="Monthly income vs expense — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrend(@RequestParam(defaultValue="6") int months) { return ResponseEntity.ok(ApiResponse.ok(dashboardService.getMonthlyTrend(Math.max(1,Math.min(months,24))))); }
    @GetMapping("/top-expenses") @Operation(summary="Top N expense categories — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getTopExpenses(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(defaultValue="5") int limit) {
        LocalDate f=from!=null?from:LocalDate.now().withDayOfMonth(1),t=to!=null?to:LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getTopExpenses(f,t,Math.max(1,Math.min(limit,20)))));
    }
    @GetMapping("/spending-by-day") @Operation(summary="Spend by day of week — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<Map<String,BigDecimal>>> getSpendingByDay(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate f=from!=null?from:LocalDate.now().minusMonths(1),t=to!=null?to:LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSpendingByDayOfWeek(f,t)));
    }
    @GetMapping("/health-score") @Operation(summary="Financial health score 0-100 — ANALYST/ADMIN")
    public ResponseEntity<ApiResponse<FinancialHealthScoreResponse>> getHealthScore() { return ResponseEntity.ok(ApiResponse.ok(healthScoreService.calculate(securityUtils.getCurrentUserId()))); }
}

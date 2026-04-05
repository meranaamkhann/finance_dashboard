package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.BudgetResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "4. Budget Management",
     description = "Create and track category budgets with real-time usage and alert status")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Create a budget for a category and period [ADMIN, ANALYST]",
               description = "Each user can have one budget per category per period. " +
                             "The response includes live `spentAmount`, `remainingAmount`, " +
                             "`usagePercent`, and `status` (ON_TRACK / WARNING / CRITICAL / EXCEEDED).")
    public ResponseEntity<ApiResponse<BudgetResponse>> create(@Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Budget created", budgetService.create(req)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "List your active budgets with live spend data [ALL ROLES]")
    public ResponseEntity<ApiResponse<PagedResponse<BudgetResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getMyBudgets(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get a single budget with live spend [ALL ROLES]")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update a budget limit or period [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @PathVariable Long id, @Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Budget updated", budgetService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Deactivate a budget [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Budget deactivated", null));
    }
}

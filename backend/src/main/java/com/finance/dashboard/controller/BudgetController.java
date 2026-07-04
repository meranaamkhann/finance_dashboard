package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.BudgetService;
import com.finance.dashboard.util.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/budgets") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')") @Tag(name="Budgets") @SecurityRequirement(name="bearerAuth")
public class BudgetController {
    private final BudgetService budgetService;
    @PostMapping @Operation(summary="Create budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> create(@Valid @RequestBody BudgetRequest req, HttpServletRequest http) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Budget created",budgetService.create(req,IpUtils.resolveIp(http)))); }
    @GetMapping @Operation(summary="My active budgets with live spend")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll() { return ResponseEntity.ok(ApiResponse.ok(budgetService.getMyBudgets())); }
    @GetMapping("/{id}") @Operation(summary="Get budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(budgetService.getById(id))); }
    @PutMapping("/{id}") @Operation(summary="Update budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(@PathVariable Long id, @Valid @RequestBody BudgetRequest req, HttpServletRequest http) { return ResponseEntity.ok(ApiResponse.ok("Updated",budgetService.update(id,req,IpUtils.resolveIp(http)))); }
    @DeleteMapping("/{id}") @Operation(summary="Deactivate budget")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest http) { budgetService.delete(id,IpUtils.resolveIp(http)); return ResponseEntity.ok(ApiResponse.ok("Deactivated",null)); }
}

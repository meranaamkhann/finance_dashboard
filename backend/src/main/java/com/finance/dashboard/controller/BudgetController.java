package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.BudgetResponse;
import com.finance.dashboard.service.BudgetService;
import com.finance.dashboard.util.IpUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
@Tag(name = "Budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @Valid @RequestBody BudgetRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Budget created", budgetService.create(req, IpUtils.resolveIp(http))));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getMyBudgets()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.getById(id)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @PathVariable Long id, @Valid @RequestBody BudgetRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", budgetService.update(id, req, IpUtils.resolveIp(http))));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest http) {
        budgetService.delete(id, IpUtils.resolveIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Deactivated", null));
    }
}

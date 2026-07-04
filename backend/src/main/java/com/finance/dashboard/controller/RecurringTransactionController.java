package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.RecurringTransactionRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.RecurringTransactionService;
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

@RestController @RequestMapping("/api/recurring") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')") @Tag(name="Recurring Transactions") @SecurityRequirement(name="bearerAuth")
public class RecurringTransactionController {
    private final RecurringTransactionService service;
    @PostMapping @Operation(summary="Create recurring rule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(@Valid @RequestBody RecurringTransactionRequest req, HttpServletRequest http) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Created",service.create(req,IpUtils.resolveIp(http)))); }
    @GetMapping @Operation(summary="My active recurring rules")
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getAll() { return ResponseEntity.ok(ApiResponse.ok(service.getMyRecurring())); }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.getById(id))); }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> update(@PathVariable Long id, @Valid @RequestBody RecurringTransactionRequest req, HttpServletRequest http) { return ResponseEntity.ok(ApiResponse.ok("Updated",service.update(id,req,IpUtils.resolveIp(http)))); }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest http) { service.delete(id,IpUtils.resolveIp(http)); return ResponseEntity.ok(ApiResponse.ok("Deactivated",null)); }
}

package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.RecurringTransactionRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.RecurringTransactionResponse;
import com.finance.dashboard.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@Tag(name = "5. Recurring Transactions",
     description = "Define rules for auto-posting transactions on a schedule. " +
                   "The scheduler runs daily at 01:00 and posts all due entries.")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Create a recurring transaction rule [ADMIN, ANALYST]",
               description = "Frequencies: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY. " +
                             "Leave `endDate` null to run indefinitely.")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(
            @Valid @RequestBody RecurringTransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Recurring rule created", recurringService.create(req)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "List your active recurring rules [ALL ROLES]")
    public ResponseEntity<ApiResponse<PagedResponse<RecurringTransactionResponse>>> getMy(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(recurringService.getMy(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get a recurring rule by ID [ALL ROLES]")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recurringService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update a recurring rule [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody RecurringTransactionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Rule updated", recurringService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Deactivate a recurring rule [ADMIN, ANALYST]")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        recurringService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Rule deactivated", null));
    }
}

package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.AuditLogResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "8. Audit Trail", description = "Immutable log of all system actions — ADMIN only")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all audit events [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getAll(page, size)));
    }

    @GetMapping("/by-actor/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Filter audit log by actor username [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByActor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getByActor(username, page, size)));
    }

    @GetMapping("/by-entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Full history for a specific entity [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long   entityId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getByEntity(entityType, entityId, page, size)));
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Filter audit log by date/time range [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getByDateRange(from, to, page, size)));
    }
}

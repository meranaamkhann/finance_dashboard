package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.AuditLogResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.service.AuditQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Trail")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {
    private final AuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditQueryService.getAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/by-actor/{username}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> byActor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByActor(username, PageRequest.of(page, size))));
    }

    @GetMapping("/by-entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> byEntity(
            @PathVariable String entityType, @PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByEntity(entityType, entityId)));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByRange(from, to, PageRequest.of(page, size))));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> byAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByAction(action, PageRequest.of(page, size))));
    }
}

package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController @RequestMapping("/api/audit") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") @Tag(name="Audit Trail") @SecurityRequirement(name="bearerAuth")
public class AuditController {
    private final AuditQueryService auditQueryService;
    @GetMapping @Operation(summary="All audit logs — ADMIN")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getAll(PageRequest.of(page,size,Sort.by("createdAt").descending())))); }
    @GetMapping("/actor/{username}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByActor(@PathVariable String username, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByActor(username,PageRequest.of(page,size)))); }
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByRange(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime from, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime to, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByRange(from,to,PageRequest.of(page,size)))); }
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getByAction(@PathVariable AuditAction action, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return ResponseEntity.ok(ApiResponse.ok(auditQueryService.getByAction(action,PageRequest.of(page,size)))); }
}

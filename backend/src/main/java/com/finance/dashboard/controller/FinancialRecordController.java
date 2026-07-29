package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.CreateRecordRequest;
import com.finance.dashboard.dto.request.UpdateRecordRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.service.AuditService;
import com.finance.dashboard.service.ExportService;
import com.finance.dashboard.service.FinancialRecordService;
import com.finance.dashboard.util.IpUtils;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {
    private final FinancialRecordService recordService;
    private final ExportService exportService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Create record — ADMIN")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> create(
            @Valid @RequestBody CreateRecordRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Record created",
                recordService.create(req, IpUtils.resolveIp(http))));
    }

    @GetMapping
    @Operation(summary = "List/search records")
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordResponse>>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Long createdById,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.ok(recordService.getAll(
                type, category, dateFrom, dateTo, keyword, tags, createdById, PageRequest.of(page, size, sort))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update record — ADMIN")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateRecordRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", recordService.update(id, req, IpUtils.resolveIp(http))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete record — ADMIN")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest http) {
        recordService.delete(id, IpUtils.resolveIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export CSV — ANALYST/ADMIN")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            HttpServletRequest http) throws Exception {
        byte[] csv = exportService.exportToCsv(type, category, from, to, keyword);
        String fn = "records_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        auditService.log(AuditAction.CSV_EXPORTED, securityUtils.getCurrentUsername(), "CSV exported", IpUtils.resolveIp(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="" + fn + """)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}

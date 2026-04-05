package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.CreateRecordRequest;
import com.finance.dashboard.dto.request.UpdateRecordRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.service.ExportService;
import com.finance.dashboard.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "3. Financial Records", description = "CRUD with filtering, search, tags, and CSV export")
public class FinancialRecordController {

    private final FinancialRecordService recordService;
    private final ExportService          exportService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record [ADMIN]")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> create(
            @Valid @RequestBody CreateRecordRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Record created", recordService.create(req, ip(http))));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(
        summary = "List records with filters and pagination [ALL ROLES]",
        description = """
            Supports combining any of the filter params:
            - `type` — INCOME or EXPENSE
            - `category` — SALARY, FOOD, HOUSING, etc.
            - `dateFrom` / `dateTo` — ISO date (yyyy-MM-dd)
            - `keyword` — full-text search in description
            - `tags` — partial match on the tags field
            - `createdById` — filter by creator user ID (ADMIN/ANALYST)
            - `sortBy` — date, amount, category, createdAt (default: date)
            - `direction` — asc or desc (default: desc)
            """
    )
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordResponse>>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category        category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Long   createdById,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "20")   int    size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getAll(type, category, dateFrom, dateTo, keyword, tags,
                        createdById, page, size, sortBy, direction)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get a record by ID [ALL ROLES]")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a record [ADMIN]")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest req,
            HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Record updated", recordService.update(id, req, ip(http))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a record [ADMIN]")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, HttpServletRequest http) {
        recordService.delete(id, ip(http));
        return ResponseEntity.ok(ApiResponse.ok("Record deleted", null));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(
        summary = "Export filtered records to CSV [ADMIN, ANALYST]",
        description = "Accepts the same filter params as GET /api/records. Returns a downloadable CSV file."
    )
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category        category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String keyword,
            HttpServletRequest http) {

        String csv      = exportService.exportToCsv(type, category, dateFrom, dateTo, keyword, ip(http));
        String filename = "finance-export-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes());
    }

    private String ip(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }
}

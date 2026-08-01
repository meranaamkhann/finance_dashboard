package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.service.FileStorageService;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/records/{recordId}/receipt")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Receipts", description = "Upload and download bill/receipt images")
@SecurityRequirement(name = "bearerAuth")
public class ReceiptController {

    private final FinancialRecordRepository recordRepository;
    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload receipt for a financial record")
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @PathVariable Long recordId,
            @RequestParam("file") MultipartFile file) {

        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", recordId));

        if (record.getReceiptPath() != null)
            fileStorageService.deleteReceipt(record.getReceiptPath());

        Long userId = securityUtils.getCurrentUserId();
        String path = fileStorageService.storeReceipt(file, userId);

        record.setReceiptPath(path);
        recordRepository.save(record);

        return ResponseEntity.ok(ApiResponse.ok("Receipt uploaded",
                Map.of("path", path)));
    }

    @GetMapping
    @Operation(summary = "Download receipt for a financial record")
    public ResponseEntity<byte[]> download(@PathVariable Long recordId) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", recordId));

        if (record.getReceiptPath() == null)
            throw new ResourceNotFoundException("No receipt attached to this record");

        byte[] data = fileStorageService.loadReceipt(record.getReceiptPath());
        String filename = "receipt-" + recordId;

        return ResponseEntity.ok()
                .headers(h -> h.setContentDisposition(
                        ContentDisposition.attachment().filename(filename).build()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping
    @Operation(summary = "Delete receipt from a financial record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long recordId) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", recordId));

        fileStorageService.deleteReceipt(record.getReceiptPath());
        record.setReceiptPath(null);
        recordRepository.save(record);

        return ResponseEntity.ok(ApiResponse.ok("Receipt deleted", null));
    }
}
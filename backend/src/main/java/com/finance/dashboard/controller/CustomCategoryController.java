package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.CustomCategoryRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.CustomCategoryResponse;
import com.finance.dashboard.service.CustomCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Custom Categories")
@SecurityRequirement(name = "bearerAuth")
public class CustomCategoryController {

    private final CustomCategoryService service;

    @GetMapping
    @Operation(summary = "Get all categories (system + workspace custom)")
    public ResponseEntity<ApiResponse<List<CustomCategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Create a custom category")
    public ResponseEntity<ApiResponse<CustomCategoryResponse>> create(
            @Valid @RequestBody CustomCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", service.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Update a custom category")
    public ResponseEntity<ApiResponse<CustomCategoryResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CustomCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated", service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Delete a custom category (system categories are protected)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }
}
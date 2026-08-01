package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.PlanResponse;
import com.finance.dashboard.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Available subscription plans")
public class PlanController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "List all visible plans — public")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.ok(planService.getVisiblePlans()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get plan by slug — public")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlan(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(planService.getPlanBySlug(slug)));
    }
}
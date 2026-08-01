package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.PlanResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Plan;
import com.finance.dashboard.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> getVisiblePlans() {
        return planRepository.findAllByVisibleTrueAndActiveTrueOrderBySortOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanBySlug(String slug) {
        return toResponse(planRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + slug)));
    }

    public Plan findBySlug(String slug) {
        return planRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + slug));
    }

    public PlanResponse toResponse(Plan p) {
        return PlanResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .description(p.getDescription())
                .monthlyPrice(p.getMonthlyPrice()).yearlyPrice(p.getYearlyPrice())
                .maxRecords(p.getMaxRecords()).maxBudgets(p.getMaxBudgets())
                .maxRecurring(p.getMaxRecurring()).maxExports(p.getMaxExports())
                .maxUsers(p.getMaxUsers()).apiAccess(p.isApiAccess())
                .prioritySupport(p.isPrioritySupport())
                .sortOrder(p.getSortOrder()).features(p.getFeatures())
                .build();
    }
}
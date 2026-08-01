package com.finance.dashboard.repository;

import com.finance.dashboard.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findAllByVisibleTrueAndActiveTrueOrderBySortOrderAsc();
    Optional<Plan> findBySlug(String slug);
    Optional<Plan> findBySlugAndActiveTrue(String slug);
}
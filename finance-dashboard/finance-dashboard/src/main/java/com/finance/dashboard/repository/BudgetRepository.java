package com.finance.dashboard.repository;

import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Page<Budget> findAllByOwnerIdAndActiveTrue(Long ownerId, Pageable pageable);

    Optional<Budget> findByIdAndOwnerId(Long id, Long ownerId);

    /** Fetch all active budgets whose period overlaps today — used by the alert engine. */
    @Query("SELECT b FROM Budget b WHERE b.active=true " +
           "AND b.periodStart <= :today AND b.periodEnd >= :today")
    List<Budget> findAllActiveBudgetsForDate(@Param("today") LocalDate today);

    Optional<Budget> findByOwnerIdAndCategoryAndPeriodStartAndPeriodEnd(
            Long ownerId, Category category, LocalDate periodStart, LocalDate periodEnd);
}

package com.finance.dashboard.repository;

import com.finance.dashboard.model.RecurringTransaction;
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
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    Page<RecurringTransaction> findAllByOwnerIdAndActiveTrue(Long ownerId, Pageable pageable);

    Optional<RecurringTransaction> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Fetch all active rules whose next execution is due.
     * A rule is due when:
     *   - it has never run (lastExecutedDate IS NULL) and startDate <= today
     *   - OR its last run was before today and it hasn't passed its endDate
     */
    @Query("SELECT r FROM RecurringTransaction r WHERE r.active = true " +
           "AND r.startDate <= :today " +
           "AND (r.endDate IS NULL OR r.endDate >= :today) " +
           "AND (r.lastExecutedDate IS NULL OR r.lastExecutedDate < :today)")
    List<RecurringTransaction> findAllDueForExecution(@Param("today") LocalDate today);
}

package com.finance.dashboard.repository;
import com.finance.dashboard.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    Optional<RecurringTransaction> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
    List<RecurringTransaction> findAllByUserIdAndActiveTrue(Long userId);
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.active=true AND rt.nextExecutionDate<=:today AND (rt.endDate IS NULL OR rt.endDate>=:today)")
    List<RecurringTransaction> findAllDueByDate(@Param("today") LocalDate today);
}

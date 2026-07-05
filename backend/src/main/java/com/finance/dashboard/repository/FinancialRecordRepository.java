package com.finance.dashboard.repository;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);
    long countByCreatedByIdAndDeletedFalse(Long userId);

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.type=:type AND r.deleted=false AND r.date BETWEEN :from AND :to")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("uid") Long uid, @Param("type") TransactionType type, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT r.category, COALESCE(SUM(r.amount),0) AS total FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.type='EXPENSE' AND r.deleted=false AND r.date BETWEEN :from AND :to GROUP BY r.category ORDER BY total DESC")
    List<Object[]> categoryBreakdownByUser(@Param("uid") Long uid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date), SUM(CASE WHEN r.type='INCOME' THEN r.amount ELSE 0 END), SUM(CASE WHEN r.type='EXPENSE' THEN r.amount ELSE 0 END) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.deleted=false AND r.date BETWEEN :from AND :to GROUP BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date) ORDER BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date)")
    List<Object[]> monthlyTrendByUser(@Param("uid") Long uid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT EXTRACT(DOW FROM r.date), COALESCE(SUM(r.amount),0) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.type='EXPENSE' AND r.deleted=false AND r.date BETWEEN :from AND :to GROUP BY EXTRACT(DOW FROM r.date) ORDER BY EXTRACT(DOW FROM r.date)")
    List<Object[]> spendingByDayOfWeekByUser(@Param("uid") Long uid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date), SUM(r.amount) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.type=:type AND r.deleted=false AND r.date>=:from GROUP BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date) ORDER BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date)")
    List<Object[]> monthlyAmountByTypeAndUser(@Param("uid") Long uid, @Param("type") TransactionType type, @Param("from") LocalDate from);

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.type='EXPENSE' AND r.category=:cat AND r.deleted=false AND r.date BETWEEN :from AND :to")
    BigDecimal spentByUserCategoryAndPeriod(@Param("uid") Long uid, @Param("cat") Category cat, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT EXTRACT(YEAR FROM r.date), EXTRACT(WEEK FROM r.date), SUM(CASE WHEN r.type='INCOME' THEN r.amount ELSE 0 END), SUM(CASE WHEN r.type='EXPENSE' THEN r.amount ELSE 0 END) FROM FinancialRecord r WHERE r.createdBy.id=:uid AND r.deleted=false AND r.date BETWEEN :from AND :to GROUP BY EXTRACT(YEAR FROM r.date), EXTRACT(WEEK FROM r.date) ORDER BY EXTRACT(YEAR FROM r.date), EXTRACT(WEEK FROM r.date)")
    List<Object[]> weeklyTrendByUser(@Param("uid") Long uid, @Param("from") LocalDate from, @Param("to") LocalDate to);
}

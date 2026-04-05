package com.finance.dashboard.repository;

import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<FinancialRecord> findAllByDeletedFalseOrderByDateDescCreatedAtDesc(Pageable pageable);

    // ── Aggregations ──────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM FinancialRecord r WHERE r.type=:type AND r.deleted=false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM FinancialRecord r " +
           "WHERE r.type=:type AND r.deleted=false AND r.date BETWEEN :from AND :to")
    BigDecimal sumByTypeAndDateRange(@Param("type") TransactionType type,
                                    @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM FinancialRecord r " +
           "WHERE r.category=:#{#cat.name()} AND r.type='EXPENSE' AND r.deleted=false " +
           "AND r.date BETWEEN :from AND :to")
    BigDecimal sumExpenseByCategoryAndPeriod(@Param("cat") com.finance.dashboard.model.Category category,
                                             @Param("from") LocalDate from, @Param("to") LocalDate to);

    // ── Analytics ─────────────────────────────────────────────────────────────

    @Query("SELECT r.category, r.type, COALESCE(SUM(r.amount),0) " +
           "FROM FinancialRecord r WHERE r.deleted=false " +
           "GROUP BY r.category, r.type ORDER BY SUM(r.amount) DESC")
    List<Object[]> getCategoryWiseTotals();

    @Query(value = "SELECT FORMATDATETIME(r.date,'yyyy-MM') AS month, r.type, SUM(r.amount) " +
                   "FROM financial_records r WHERE r.deleted=false AND r.date>=:from " +
                   "GROUP BY FORMATDATETIME(r.date,'yyyy-MM'), r.type ORDER BY month DESC",
           nativeQuery = true)
    List<Object[]> getMonthlyTotals(@Param("from") LocalDate from);

    @Query(value = "SELECT WEEK(r.date), YEAR(r.date), r.type, SUM(r.amount) " +
                   "FROM financial_records r WHERE r.deleted=false AND r.date>=:from " +
                   "GROUP BY WEEK(r.date), YEAR(r.date), r.type " +
                   "ORDER BY YEAR(r.date) DESC, WEEK(r.date) DESC",
           nativeQuery = true)
    List<Object[]> getWeeklyTotals(@Param("from") LocalDate from);

    /** Day-of-week spending pattern (0=Sunday … 6=Saturday). */
    @Query(value = "SELECT DAY_OF_WEEK(r.date)-1 AS dow, SUM(r.amount) " +
                   "FROM financial_records r WHERE r.deleted=false AND r.type='EXPENSE' " +
                   "GROUP BY DAY_OF_WEEK(r.date) ORDER BY dow",
           nativeQuery = true)
    List<Object[]> getSpendingByDayOfWeek();

    /** Top N categories by total expense in a date range. */
    @Query(value = "SELECT r.category, SUM(r.amount) AS total " +
                   "FROM financial_records r WHERE r.deleted=false AND r.type='EXPENSE' " +
                   "AND r.date BETWEEN :from AND :to " +
                   "GROUP BY r.category ORDER BY total DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> getTopExpenseCategories(@Param("from") LocalDate from,
                                           @Param("to") LocalDate to,
                                           @Param("limit") int limit);

    /** Month-over-month change for a given type. */
    @Query(value = "SELECT FORMATDATETIME(r.date,'yyyy-MM'), SUM(r.amount) " +
                   "FROM financial_records r WHERE r.deleted=false AND r.type=:type " +
                   "GROUP BY FORMATDATETIME(r.date,'yyyy-MM') ORDER BY 1 DESC LIMIT :months",
           nativeQuery = true)
    List<Object[]> getMonthlyTotalsByType(@Param("type") String type, @Param("months") int months);
}

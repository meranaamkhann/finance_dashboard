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

    // ── Totals ────────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.type = :type " +
           "AND r.deleted = false AND r.date BETWEEN :from AND :to")
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("uid") Long uid,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ── Category breakdown ─────────────────────────────────────────────────────

    @Query("SELECT r.category, COALESCE(SUM(r.amount), 0) AS total " +
           "FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.type = 'EXPENSE' " +
           "AND r.deleted = false AND r.date BETWEEN :from AND :to " +
           "GROUP BY r.category ORDER BY total DESC")
    List<Object[]> categoryBreakdownByUser(
            @Param("uid") Long uid,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ── Monthly trend (JPQL — EXTRACT YEAR/MONTH valid in Hibernate 6) ────────

    @Query("SELECT EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date), " +
           "SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END), " +
           "SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END) " +
           "FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.deleted = false " +
           "AND r.date BETWEEN :from AND :to " +
           "GROUP BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date) " +
           "ORDER BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date)")
    List<Object[]> monthlyTrendByUser(
            @Param("uid") Long uid,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ── Spending by day of week ────────────────────────────────────────────────
    // Hibernate 6 HQL syntax: "extract(day of week from x)" — returns 1=Sun..7=Sat

    @Query("SELECT extract(day of week from r.date), COALESCE(SUM(r.amount), 0) " +
           "FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.type = 'EXPENSE' " +
           "AND r.deleted = false AND r.date BETWEEN :from AND :to " +
           "GROUP BY extract(day of week from r.date) " +
           "ORDER BY extract(day of week from r.date)")
    List<Object[]> spendingByDayOfWeekByUser(
            @Param("uid") Long uid,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ── Health score: monthly amounts by type ─────────────────────────────────

    @Query("SELECT EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date), SUM(r.amount) " +
           "FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.type = :type " +
           "AND r.deleted = false AND r.date >= :from " +
           "GROUP BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date) " +
           "ORDER BY EXTRACT(YEAR FROM r.date), EXTRACT(MONTH FROM r.date)")
    List<Object[]> monthlyAmountByTypeAndUser(
            @Param("uid") Long uid,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from);

    // ── Budget spend ──────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.createdBy.id = :uid AND r.type = 'EXPENSE' " +
           "AND r.category = :cat AND r.deleted = false " +
           "AND r.date BETWEEN :from AND :to")
    BigDecimal spentByUserCategoryAndPeriod(
            @Param("uid") Long uid,
            @Param("cat") Category cat,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ── Weekly trend (nativeQuery — H2 MODE=PostgreSQL + PostgreSQL both support this) ──

    @Query(value = "SELECT EXTRACT(YEAR FROM date) as yr, EXTRACT(WEEK FROM date) as wk, " +
                   "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income, " +
                   "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense " +
                   "FROM financial_records " +
                   "WHERE created_by_id = :uid AND deleted = false " +
                   "AND date BETWEEN :from AND :to " +
                   "GROUP BY EXTRACT(YEAR FROM date), EXTRACT(WEEK FROM date) " +
                   "ORDER BY EXTRACT(YEAR FROM date), EXTRACT(WEEK FROM date)",
           nativeQuery = true)
    List<Object[]> weeklyTrendByUser(
            @Param("uid") Long uid,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

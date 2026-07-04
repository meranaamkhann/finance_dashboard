package com.finance.dashboard.repository;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
    List<Budget> findAllByUserIdAndActiveTrue(Long userId);
    List<Budget> findAllByActiveTrue();
    @Query("SELECT b FROM Budget b WHERE b.user.id=:uid AND b.category=:cat AND b.active=true AND b.periodStart<=:date AND b.periodEnd>=:date")
    List<Budget> findActiveBudgetsForUserCategoryAndDate(@Param("uid") Long uid, @Param("cat") Category cat, @Param("date") LocalDate date);
    boolean existsByUserIdAndCategoryAndActiveTrueAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(Long userId, Category category, LocalDate end, LocalDate start);
}

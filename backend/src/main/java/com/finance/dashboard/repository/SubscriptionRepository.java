package com.finance.dashboard.repository;

import com.finance.dashboard.model.Subscription;
import com.finance.dashboard.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :uid " +
           "AND s.status IN ('ACTIVE','TRIAL') " +
           "AND s.endDate >= :today ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveByUserId(
            @Param("uid") Long uid, @Param("today") LocalDate today);

    List<Subscription> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.autoRenew = true " +
           "AND s.status = 'ACTIVE' AND s.endDate = :date")
    List<Subscription> findDueForRenewal(@Param("date") LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.endDate < :today")
    List<Subscription> findExpired(@Param("today") LocalDate today);
}
package com.finance.dashboard.repository;
import com.finance.dashboard.model.Notification;
import com.finance.dashboard.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndReadFalse(Long userId);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    @Modifying @Query("UPDATE Notification n SET n.read=true, n.readAt=:now WHERE n.user.id=:uid AND n.read=false")
    int markAllReadByUserId(@Param("uid") Long uid, @Param("now") LocalDateTime now);
    boolean existsByUserIdAndTypeAndReadFalseAndCreatedAtAfterAndMessageContaining(Long userId, NotificationType type, LocalDateTime after, String msg);
}

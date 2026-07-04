package com.finance.dashboard.model;
import com.finance.dashboard.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user_unread", columnList = "user_id, read")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private NotificationType type;
    @Column(nullable = false, length = 500) private String message;
    @Column(nullable = false) @Builder.Default private boolean read = false;
    @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    private LocalDateTime readAt;
}

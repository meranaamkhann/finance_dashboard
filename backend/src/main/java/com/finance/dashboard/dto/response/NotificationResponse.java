package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt, readAt;
}

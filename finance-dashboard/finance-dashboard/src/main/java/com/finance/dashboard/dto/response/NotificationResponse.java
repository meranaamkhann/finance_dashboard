package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder
public class NotificationResponse {
    private Long          id;
    private String        title;
    private String        message;
    private String        type;
    private boolean       isRead;
    private LocalDateTime createdAt;
}

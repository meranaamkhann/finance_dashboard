package com.finance.dashboard.service;
import com.finance.dashboard.dto.response.NotificationResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Notification;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.NotificationType;
import com.finance.dashboard.repository.NotificationRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repo;
    private final SecurityUtils securityUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(User user, NotificationType type, String message) {
        repo.save(Notification.builder().user(user).type(type).message(message).build());
    }

    @Transactional(readOnly=true)
    public PagedResponse<NotificationResponse> getMyNotifications(boolean unreadOnly, Pageable pageable) {
        Long uid = securityUtils.getCurrentUserId();
        var page = unreadOnly ? repo.findByUserIdAndReadFalseOrderByCreatedAtDesc(uid, pageable)
                              : repo.findByUserIdOrderByCreatedAtDesc(uid, pageable);
        return new PagedResponse<>(page.map(this::toResponse));
    }

    @Transactional(readOnly=true)
    public long getUnreadCount() { return repo.countByUserIdAndReadFalse(securityUtils.getCurrentUserId()); }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Long uid = securityUtils.getCurrentUserId();
        Notification n = repo.findByIdAndUserId(id, uid).orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!n.isRead()) { n.setRead(true); n.setReadAt(LocalDateTime.now()); repo.save(n); }
        return toResponse(n);
    }

    @Transactional
    public int markAllRead() { return repo.markAllReadByUserId(securityUtils.getCurrentUserId(), LocalDateTime.now()); }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder().id(n.getId()).type(n.getType()).message(n.getMessage())
                .read(n.isRead()).createdAt(n.getCreatedAt()).readAt(n.getReadAt()).build();
    }
}

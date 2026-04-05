package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.NotificationResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Notification;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.NotificationRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final UserRepository         userRepo;

    // ── Internal creation (called by budget / recurring services) ─────────────

    @Transactional
    public void send(Long userId, String title, String message, String type) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        notifRepo.save(Notification.builder()
                .user(user).title(title).message(message).type(type).isRead(false).build());
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getForUser(Long userId, boolean unreadOnly,
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> result = unreadOnly
                ? notifRepo.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notifRepo.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.from(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notifRepo.countByUserIdAndIsReadFalse(userId);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Transactional
    public int markAllRead(Long userId) {
        return notifRepo.markAllReadForUser(userId);
    }

    @Transactional
    public void markOneRead(Long notifId, Long userId) {
        Notification n = notifRepo.findById(notifId)
                .filter(x -> x.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notifId));
        n.setRead(true);
        notifRepo.save(n);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).isRead(n.isRead()).createdAt(n.getCreatedAt())
                .build();
    }
}

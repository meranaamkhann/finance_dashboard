package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.NotificationResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.service.NotificationService;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "7. Notifications", description = "In-app notifications for budget alerts and recurring events")
public class NotificationController {

    private final NotificationService notifService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get your notifications [ALL ROLES]")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getAll(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                notifService.getForUser(SecurityUtils.currentUserId(), unreadOnly, page, size)));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Count unread notifications [ALL ROLES]")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseEntity.ok(ApiResponse.ok(notifService.countUnread(SecurityUtils.currentUserId())));
    }

    @PatchMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read [ALL ROLES]")
    public ResponseEntity<ApiResponse<Integer>> markAllRead() {
        return ResponseEntity.ok(ApiResponse.ok("Marked as read",
                notifService.markAllRead(SecurityUtils.currentUserId())));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a single notification as read [ALL ROLES]")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notifService.markOneRead(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }
}

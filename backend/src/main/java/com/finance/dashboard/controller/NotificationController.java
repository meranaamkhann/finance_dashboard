package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
@Tag(name="Notifications") @SecurityRequirement(name="bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping @Operation(summary="Get my notifications")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getAll(@RequestParam(defaultValue="false") boolean unreadOnly, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getMyNotifications(unreadOnly,PageRequest.of(page,size,Sort.by("createdAt").descending()))));
    }
    @GetMapping("/unread-count") @Operation(summary="Unread notification count")
    public ResponseEntity<ApiResponse<Map<String,Long>>> unreadCount() { return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount",notificationService.getUnreadCount()))); }
    @PatchMapping("/{id}/read") @Operation(summary="Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok("Marked read",notificationService.markRead(id))); }
    @PatchMapping("/mark-all-read") @Operation(summary="Mark all as read")
    public ResponseEntity<ApiResponse<Map<String,Integer>>> markAllRead() { return ResponseEntity.ok(ApiResponse.ok(Map.of("marked",notificationService.markAllRead()))); }
}

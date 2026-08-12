package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.InviteMemberRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.WorkspaceMemberResponse;
import com.finance.dashboard.dto.response.WorkspaceResponse;
import com.finance.dashboard.model.Workspace;
import com.finance.dashboard.model.WorkspaceMember;
import com.finance.dashboard.model.enums.WorkspaceMemberRole;
import com.finance.dashboard.service.SubscriptionService;
import com.finance.dashboard.service.WorkspaceService;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workspace")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Workspace", description = "Workspace management and member invitations")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService    workspaceService;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils       securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getMyWorkspace() {
        Workspace ws = workspaceService.getMyWorkspace();
        List<WorkspaceMember> members = workspaceService.getMembers();

        int maxUsers = 1;
        try {
            maxUsers = subscriptionService
                    .getActivePlan(securityUtils.getCurrentUserId()).getMaxUsers();
        } catch (Exception e) {
            maxUsers = 1;
        }

        WorkspaceResponse res = WorkspaceResponse.builder()
                .id(ws.getId())
                .name(ws.getName())
                .ownerUsername(ws.getOwner().getUsername())
                .ownerFullName(ws.getOwner().getFullName())
                .memberCount(members.size())
                .maxMembers(maxUsers)
                .members(members.stream().map(this::toMemberResponse).toList())
                .createdAt(ws.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping("/members")
    @Operation(summary = "Invite a member to your workspace")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> invite(
            @Valid @RequestBody InviteMemberRequest req) {
        WorkspaceMember member = workspaceService.inviteMember(req.getEmail(), req.getRole());
        return ResponseEntity.ok(ApiResponse.ok("Member invited successfully",
                toMemberResponse(member)));
    }

    @PutMapping("/members/{userId}/role")
    @Operation(summary = "Change a member's role")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable Long userId,
            @RequestParam WorkspaceMemberRole role) {
        workspaceService.updateMemberRole(userId, role);
        return ResponseEntity.ok(ApiResponse.ok("Role updated", null));
    }

    @DeleteMapping("/members/{userId}")
    @Operation(summary = "Remove a member from your workspace")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long userId) {
        workspaceService.removeMember(userId);
        return ResponseEntity.ok(ApiResponse.ok("Member removed", null));
    }

    private WorkspaceMemberResponse toMemberResponse(WorkspaceMember m) {
        return WorkspaceMemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .username(m.getUser().getUsername())
                .fullName(m.getUser().getFullName())
                .email(m.getUser().getEmail())
                .avatarUrl(m.getUser().getAvatarUrl())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
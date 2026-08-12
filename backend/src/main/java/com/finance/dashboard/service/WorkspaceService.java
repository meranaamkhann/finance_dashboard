package com.finance.dashboard.service;

import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.exception.SubscriptionLimitException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.Workspace;
import com.finance.dashboard.model.WorkspaceMember;
import com.finance.dashboard.model.enums.WorkspaceMemberRole;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.repository.WorkspaceMemberRepository;
import com.finance.dashboard.repository.WorkspaceRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository      workspaceRepo;
    private final WorkspaceMemberRepository memberRepo;
    private final UserRepository            userRepo;
    private final SubscriptionService       subscriptionService;
    private final SecurityUtils             securityUtils;

    @Transactional
    public Workspace createForUser(User user) {
        if (workspaceRepo.existsByOwnerId(user.getId())) {
            return workspaceRepo.findByOwnerId(user.getId()).orElseThrow();
        }
        Workspace ws = workspaceRepo.save(Workspace.builder()
                .name(user.getFullName() + "'s Workspace")
                .owner(user)
                .build());
        memberRepo.save(WorkspaceMember.builder()
                .workspace(ws)
                .user(user)
                .role(WorkspaceMemberRole.OWNER)
                .build());
        log.info("Workspace created for user: {}", user.getUsername());
        return ws;
    }

    @Transactional(readOnly = true)
    public Workspace getMyWorkspace() {
        Long uid = securityUtils.getCurrentUserId();
        return workspaceRepo.findByOwnerId(uid)
                .or(() -> memberRepo.findAllByUserId(uid).stream()
                        .map(WorkspaceMember::getWorkspace)
                        .findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No workspace found"));
    }

    @Transactional(readOnly = true)
    public Long getMyWorkspaceId() {
        return getMyWorkspace().getId();
    }

    @Transactional
    public WorkspaceMember inviteMember(String email, WorkspaceMemberRole role) {
        User currentUser = securityUtils.getCurrentUser();
        Workspace ws     = workspaceRepo.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Only workspace owners can invite members"));

        com.finance.dashboard.model.Plan plan = subscriptionService.getActivePlan(currentUser.getId());
        int current = memberRepo.countByWorkspaceId(ws.getId());
        if (current >= plan.getMaxUsers()) {
            throw new SubscriptionLimitException(
                "Your plan allows " + plan.getMaxUsers() + " member(s). Upgrade to add more.");
        }

        User invitee = userRepo.findByEmailAndDeletedFalse(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with email: " + email + ". They must register first."));

        if (memberRepo.existsByWorkspaceIdAndUserId(ws.getId(), invitee.getId())) {
            throw new BadRequestException("User is already a member of this workspace");
        }

        if (invitee.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You are already the owner of this workspace");
        }

        WorkspaceMember member = memberRepo.save(WorkspaceMember.builder()
                .workspace(ws)
                .user(invitee)
                .role(role)
                .invitedBy(currentUser)
                .build());

        log.info("User {} invited {} as {} to workspace {}",
                currentUser.getUsername(), email, role, ws.getId());
        return member;
    }

    @Transactional
    public void removeMember(Long userId) {
        Long ownerId = securityUtils.getCurrentUserId();
        Workspace ws = workspaceRepo.findByOwnerId(ownerId)
                .orElseThrow(() -> new BadRequestException("Only workspace owners can remove members"));

        if (userId.equals(ownerId)) {
            throw new BadRequestException("Cannot remove yourself from your own workspace");
        }

        memberRepo.deleteByWorkspaceIdAndUserId(ws.getId(), userId);
        log.info("User {} removed from workspace {}", userId, ws.getId());
    }

    @Transactional
    public void updateMemberRole(Long userId, WorkspaceMemberRole newRole) {
        Long ownerId = securityUtils.getCurrentUserId();
        Workspace ws = workspaceRepo.findByOwnerId(ownerId)
                .orElseThrow(() -> new BadRequestException("Only workspace owners can change roles"));

        if (newRole == WorkspaceMemberRole.OWNER) {
            throw new BadRequestException("Cannot assign OWNER role to members");
        }

        WorkspaceMember member = memberRepo.findByWorkspaceIdAndUserId(ws.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setRole(newRole);
        memberRepo.save(member);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMember> getMembers() {
        Workspace ws = getMyWorkspace();
        return memberRepo.findAllByWorkspaceId(ws.getId());
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceMember> getMembership(Long workspaceId, Long userId) {
        return memberRepo.findByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Transactional(readOnly = true)
    public boolean hasAccessTo(Long workspaceId) {
        Long uid = securityUtils.getCurrentUserId();
        return memberRepo.existsByWorkspaceIdAndUserId(workspaceId, uid);
    }
}
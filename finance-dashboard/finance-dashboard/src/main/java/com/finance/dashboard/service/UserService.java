package com.finance.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepo;
    private final PasswordEncoder encoder;
    private final AuditService    auditService;
    private final ObjectMapper    objectMapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(CreateUserRequest req, String ip) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new DuplicateResourceException("Username '" + req.getUsername() + "' is already taken.");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Email '" + req.getEmail() + "' is already registered.");

        User saved = userRepo.save(User.builder()
                .username(req.getUsername()).email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .fullName(req.getFullName()).role(req.getRole()).active(true).build());

        auditService.log(SecurityUtils.currentUsername(), AuditAction.CREATE,
                "User", saved.getId(), "Created user: " + saved.getUsername(),
                null, toJson(toResponse(saved)), ip);

        return toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAll(int page, int size, String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                                 : Sort.by(sortBy).ascending();
        return PagedResponse.from(userRepo.findAll(PageRequest.of(page, size, sort))
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getByRole(Role role, int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(userRepo.findAllByRole(role, p).map(this::toResponse));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req, String ip) {
        User user    = findOrThrow(id);
        String before = toJson(toResponse(user));

        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepo.existsByEmail(req.getEmail()))
                throw new DuplicateResourceException("Email '" + req.getEmail() + "' is already in use.");
            user.setEmail(req.getEmail());
        }
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getRole()     != null) user.setRole(req.getRole());
        if (req.getActive()   != null) user.setActive(req.getActive());
        if (req.getPassword() != null) user.setPassword(encoder.encode(req.getPassword()));

        User saved = userRepo.save(user);
        auditService.log(SecurityUtils.currentUsername(), AuditAction.UPDATE,
                "User", id, "Updated user: " + saved.getUsername(),
                before, toJson(toResponse(saved)), ip);

        return toResponse(saved);
    }

    @Transactional
    public UserResponse toggleStatus(Long id, boolean active, String ip) {
        User user = findOrThrow(id);
        if (user.getId().equals(SecurityUtils.currentUserId()))
            throw new BadRequestException("You cannot deactivate your own account.");
        user.setActive(active);
        User saved = userRepo.save(user);
        String action = active ? "Activated" : "Deactivated";
        auditService.log(SecurityUtils.currentUsername(), AuditAction.UPDATE,
                "User", id, action + " user: " + saved.getUsername(), null, null, ip);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, String ip) {
        User user = findOrThrow(id);
        if (user.getId().equals(SecurityUtils.currentUserId()))
            throw new BadRequestException("You cannot delete your own account.");
        user.setActive(false);
        userRepo.save(user);
        auditService.log(SecurityUtils.currentUsername(), AuditAction.DELETE,
                "User", id, "Soft-deleted user: " + user.getUsername(), null, null, ip);
    }

    // ── Mapper & helpers ──────────────────────────────────────────────────────

    public UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId()).username(u.getUsername()).email(u.getEmail())
                .fullName(u.getFullName()).role(u.getRole()).active(u.isActive())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt())
                .build();
    }

    private User findOrThrow(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}

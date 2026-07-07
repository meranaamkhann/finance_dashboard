package com.finance.dashboard.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.*;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.exception.*;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserResponse create(CreateUserRequest req, String actor, String ip) {
        if (userRepository.existsByUsernameAndDeletedFalse(req.getUsername().toLowerCase()))
            throw new DuplicateResourceException("Username taken: " + req.getUsername());
        if (userRepository.existsByEmailAndDeletedFalse(req.getEmail().toLowerCase()))
            throw new DuplicateResourceException("Email registered: " + req.getEmail());
        User user = User.builder().username(req.getUsername().toLowerCase().trim())
                .email(req.getEmail().toLowerCase().trim()).fullName(req.getFullName().trim())
                .password(passwordEncoder.encode(req.getPassword())).role(req.getRole()).build();
        userRepository.save(user);
        auditService.log(AuditAction.USER_CREATED, actor, "User", user.getId(), null, safeJson(user), ip, "Created: " + user.getUsername());
        return toResponse(user);
    }

    @Transactional(readOnly=true) public PagedResponse<UserResponse> getAll(Pageable p) { return new PagedResponse<>(userRepository.findAllByDeletedFalse(p).map(this::toResponse)); }
    @Transactional(readOnly=true) public UserResponse getById(Long id) { return toResponse(findActive(id)); }
    @Transactional(readOnly=true) public PagedResponse<UserResponse> getByRole(Role r, Pageable p) { return new PagedResponse<>(userRepository.findAllByRoleAndDeletedFalse(r,p).map(this::toResponse)); }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req, String actor, String ip) {
        User user = findActive(id); String before = safeJson(user);
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            String norm = req.getEmail().toLowerCase().trim();
            if (userRepository.existsByEmailAndDeletedFalse(norm)) throw new DuplicateResourceException("Email taken: " + norm);
            user.setEmail(norm);
        }
        if (req.getFullName() != null && !req.getFullName().isBlank()) user.setFullName(req.getFullName().trim());
        if (req.getRole() != null) user.setRole(req.getRole());
        userRepository.save(user);
        auditService.log(AuditAction.USER_UPDATED, actor, "User", id, before, safeJson(user), ip, "Updated: " + user.getUsername());
        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req, String actor, String ip) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) throw new BadRequestException("Passwords do not match");
        User user = findActive(id);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) throw new BadRequestException("Current password is incorrect");
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) throw new BadRequestException("New password must differ from current");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);
        auditService.log(AuditAction.PASSWORD_CHANGED, actor, "User", id, null, null, ip, "Password changed: " + user.getUsername());
    }

    @Transactional
    public void activate(Long id, String actor, String ip) { User u=findActive(id); if(u.isActive()) throw new BadRequestException("Already active"); u.setActive(true); userRepository.save(u); auditService.log(AuditAction.USER_ACTIVATED,actor,"User",id,null,null,ip,"Activated: "+u.getUsername()); }
    @Transactional
    public void deactivate(Long id, String actor, String ip) { User u=findActive(id); if(!u.isActive()) throw new BadRequestException("Already inactive"); u.setActive(false); userRepository.save(u); auditService.log(AuditAction.USER_DEACTIVATED,actor,"User",id,null,null,ip,"Deactivated: "+u.getUsername()); }
    @Transactional
    public void delete(Long id, String actor, String ip) { User u=findActive(id); String b=safeJson(u); u.setDeleted(true); u.setActive(false); userRepository.save(u); auditService.log(AuditAction.USER_DELETED,actor,"User",id,b,null,ip,"Deleted: "+u.getUsername()); }

    private User findActive(Long id) { return userRepository.findByIdAndDeletedFalse(id).orElseThrow(()->new ResourceNotFoundException("User",id)); }
    public UserResponse toResponse(User u) { return UserResponse.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).fullName(u.getFullName()).role(u.getRole()).active(u.isActive()).createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt()).build(); }
    private String safeJson(Object o) { try{return objectMapper.writeValueAsString(o);}catch(Exception e){return "{}";} }

    public UserResponse createUser(CreateUserRequest req, String actor) {
    return create(req, actor, "127.0.0.1");
}
}

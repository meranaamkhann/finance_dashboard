package com.finance.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock UserRepository  userRepo;
    @Mock PasswordEncoder encoder;
    @Mock AuditService    auditService;
    @Mock ObjectMapper    objectMapper;
    @InjectMocks UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).username("testuser").email("test@example.com")
                .password("encoded").fullName("Test User").role(Role.VIEWER).active(true).build();
        mockSecurityContext("admin");
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    @DisplayName("createUser — happy path persists and returns response")
    void createUser_Success() {
        CreateUserRequest req = buildCreateRequest("testuser", "test@example.com");
        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.existsByEmail("test@example.com")).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encoded");
        when(userRepo.save(any())).thenReturn(sampleUser);

        UserResponse res = userService.createUser(req, "127.0.0.1");

        assertThat(res.getUsername()).isEqualTo("testuser");
        assertThat(res.getRole()).isEqualTo(Role.VIEWER);
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("createUser — duplicate username throws DuplicateResourceException")
    void createUser_DuplicateUsername() {
        CreateUserRequest req = buildCreateRequest("testuser", "other@example.com");
        when(userRepo.existsByUsername("testuser")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(req, "127.0.0.1"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("testuser");
    }

    @Test
    @DisplayName("createUser — duplicate email throws DuplicateResourceException")
    void createUser_DuplicateEmail() {
        CreateUserRequest req = buildCreateRequest("newuser", "test@example.com");
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("test@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(req, "127.0.0.1"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@example.com");
    }

    @Test
    @DisplayName("getById — returns user when found")
    void getById_Found() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        UserResponse res = userService.getById(1L);
        assertThat(res.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById — throws ResourceNotFoundException when missing")
    void getById_NotFound() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update — partial update only changes provided fields")
    void update_PartialFields() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole(Role.ANALYST);
        req.setFullName("Updated Name");

        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse res = userService.update(1L, req, "127.0.0.1");
        assertThat(res.getRole()).isEqualTo(Role.ANALYST);
        assertThat(res.getFullName()).isEqualTo("Updated Name");
        assertThat(res.getEmail()).isEqualTo("test@example.com"); // unchanged
    }

    @Test
    @DisplayName("delete — sets user inactive (soft delete)")
    void delete_SoftDeletes() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        userService.delete(1L, "127.0.0.1");
        assertThat(sampleUser.isActive()).isFalse();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateUserRequest buildCreateRequest(String username, String email) {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername(username); req.setEmail(email);
        req.setPassword("Pass@1234"); req.setFullName("Test"); req.setRole(Role.VIEWER);
        return req;
    }

    private void mockSecurityContext(String username) {
        User admin = User.builder().id(99L).username(username).email("a@b.com")
                .password("x").role(Role.ADMIN).active(true).build();
        UserDetailsImpl principal = new UserDetailsImpl(admin);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}

package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.*;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.service.UserService;
import com.finance.dashboard.util.IpUtils;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
@Tag(name="Users") @SecurityRequirement(name="bearerAuth")
public class UserController {
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me") @Operation(summary="Get own profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() { return ResponseEntity.ok(ApiResponse.ok(userService.getById(securityUtils.getCurrentUserId()))); }

    @PutMapping("/me") @Operation(summary="Update own profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(@Valid @RequestBody UpdateUserRequest req, HttpServletRequest http) {
        if (!securityUtils.getCurrentUserDetails().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))) req.setRole(null);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",userService.update(securityUtils.getCurrentUserId(),req,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http))));
    }

    @PostMapping("/me/change-password") @Operation(summary="Change own password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpServletRequest http) {
        userService.changePassword(securityUtils.getCurrentUserId(),req,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Password changed",null));
    }

    @PostMapping @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Create user — ADMIN")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created",userService.create(req,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http))));
    }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") @Operation(summary="List users — ADMIN")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="createdAt") String sort, @RequestParam(defaultValue="desc") String dir) {
        Sort s=dir.equalsIgnoreCase("asc")?Sort.by(sort).ascending():Sort.by(sort).descending();
        return ResponseEntity.ok(ApiResponse.ok(userService.getAll(PageRequest.of(page,size,s))));
    }
    @GetMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Get user by ID — ADMIN")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(userService.getById(id))); }
    @GetMapping("/role/{role}") @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Filter by role — ADMIN")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getByRole(@PathVariable Role role, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ResponseEntity.ok(ApiResponse.ok(userService.getByRole(role,PageRequest.of(page,size)))); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @Operation(summary="Update user — ADMIN")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req, HttpServletRequest http) { return ResponseEntity.ok(ApiResponse.ok("Updated",userService.update(id,req,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http)))); }
    @PatchMapping("/{id}/activate") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id, HttpServletRequest http) { userService.activate(id,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http)); return ResponseEntity.ok(ApiResponse.ok("User activated",null)); }
    @PatchMapping("/{id}/deactivate") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id, HttpServletRequest http) { userService.deactivate(id,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http)); return ResponseEntity.ok(ApiResponse.ok("User deactivated",null)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest http) { userService.delete(id,securityUtils.getCurrentUsername(),IpUtils.resolveIp(http)); return ResponseEntity.ok(ApiResponse.ok("User deleted",null)); }
}

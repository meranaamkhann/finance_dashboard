package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreateUserRequest {
    @NotBlank @Size(min=3,max=50) @Pattern(regexp="^[a-zA-Z0-9_]+$", message="Username: letters, digits, underscores only") private String username;
    @NotBlank @Email @Size(max=100) private String email;
    @NotBlank @Size(min=2,max=100) private String fullName;
    @NotBlank @Size(min=8,max=64) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\-]).{8,64}$", message="Password needs uppercase, lowercase, digit and special char") private String password;
    @NotNull private Role role;
}

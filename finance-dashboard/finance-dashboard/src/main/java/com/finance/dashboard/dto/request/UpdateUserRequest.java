package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Invalid email address")
    private String email;

    @Size(max = 100)
    private String fullName;

    private Role role;

    private Boolean active;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
             message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;
}

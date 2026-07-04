package com.finance.dashboard.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank @Size(min=8,max=64) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\-]).{8,64}$", message="Password needs uppercase, lowercase, digit and special char") private String newPassword;
    @NotBlank private String confirmPassword;
}

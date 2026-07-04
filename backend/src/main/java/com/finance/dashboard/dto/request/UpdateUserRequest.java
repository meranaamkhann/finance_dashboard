package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class UpdateUserRequest {
    @Size(min=2,max=100) private String fullName;
    @Email @Size(max=100) private String email;
    private Role role;
}

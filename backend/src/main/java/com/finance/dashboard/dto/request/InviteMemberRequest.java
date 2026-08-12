package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.enums.WorkspaceMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteMemberRequest {

    @NotBlank @Email(message = "Valid email is required")
    private String email;

    @NotNull(message = "Role is required")
    private WorkspaceMemberRole role;
}
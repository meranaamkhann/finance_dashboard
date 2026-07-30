package com.finance.dashboard.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginByEmailRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}

package org.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Login annot be empty")
    String login;

    @NotBlank(message = "Password cannot be empty")
    String password;

    @NotBlank(message = "User ID cannot be empty")
    Long userId;

    @NotBlank(message = "Role cannot be empty")
    @Pattern(
            regexp = "ROLE_USER|ROLE_ADMIN",
            message = "Role must be ROLE_USER or ROLE_ADMIN"
    )
    String role;
}

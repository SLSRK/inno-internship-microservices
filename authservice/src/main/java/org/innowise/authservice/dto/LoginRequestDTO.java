package org.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Login cannot be empty")
    private String login;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}

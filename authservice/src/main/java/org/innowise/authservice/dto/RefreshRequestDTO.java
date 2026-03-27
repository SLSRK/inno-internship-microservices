package org.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshRequestDTO {

    @NotBlank(message = "Token cannot be empty")
    private String refreshToken;
}

package org.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidateRequestDTO {

    @NotBlank(message = "Token cannot be empty")
    private String token;
}

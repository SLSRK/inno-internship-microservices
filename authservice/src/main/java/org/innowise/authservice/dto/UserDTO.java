package org.innowise.authservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "Cannot be empty")
    @Size(max = 100, message = "The name is too long")
    private String name;

    @NotBlank(message = "Cannot be empty")
    @Size(max = 100, message = "The surname is too long")
    private String surname;

    @PastOrPresent(message = "Birth date cannot be in the future")
    private LocalDate birthDate;

    @Valid
    private Boolean active;

    @Email(message = "Invalid email")
    @NotBlank(message = "Cannot be empty")
    @Size(max = 100, message = "The email is too long")
    private String email;
}

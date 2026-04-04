package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private Boolean active;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

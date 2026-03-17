package org.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Cannot be empty")
    @Size(min = 16, max = 16, message = "Card number must contain 16 chars")
    private String number;

    @NotBlank(message = "Cannot be empty")
    @Size(max = 100, message = "Too long")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "ID cannot be null")
    private Long userId;
}

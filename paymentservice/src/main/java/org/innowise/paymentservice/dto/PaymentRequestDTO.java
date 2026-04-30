package org.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Order id cannot be null")
    private Long orderId;

    @NotNull(message = "User id cannot be null")
    private Long userId;

    private Instant timestamp;

    @NotNull(message = "Payment amount cannot be null")
    private Long paymentAmount;
}

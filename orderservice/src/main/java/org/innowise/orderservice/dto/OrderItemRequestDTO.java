package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class OrderItemRequestDTO {

    Long orderId;

    @NotNull(message = "Item id cannot be null")
    Long itemId;

    @NotNull(message = "Quantity cannot be null")
    Long quantity;

}

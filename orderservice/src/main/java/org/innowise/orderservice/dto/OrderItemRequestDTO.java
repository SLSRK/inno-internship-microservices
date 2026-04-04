package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {

    Long orderId;

    @NotNull(message = "Item id cannot be null")
    Long itemId;

    @NotNull(message = "Quantity cannot be null")
    Long quantity;

}

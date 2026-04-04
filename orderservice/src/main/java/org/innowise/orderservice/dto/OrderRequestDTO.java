package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderRequestDTO {

    @NotNull
    Long userId;

    @NotEmpty(message = "Order cannot be without items")
    List<OrderItemRequestDTO> orderItems;
}

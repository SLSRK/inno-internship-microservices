package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderUpdateDTO {

    @NotNull(message = "User ID cannot be null")
    Long userId;
    @NotNull(message = "Status cannot be empty")
    String status;

    @NotEmpty(message = "Order cannot be without items")
    List<OrderItemRequestDTO> orderItems;
}

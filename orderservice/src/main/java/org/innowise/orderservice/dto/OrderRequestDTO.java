package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderRequestDTO {

    @NotNull
    Long userId;

    List<OrderItemRequestDTO> orderItems;
}

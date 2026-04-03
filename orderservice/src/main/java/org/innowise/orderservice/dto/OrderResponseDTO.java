package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {

    Long id;

    Long userId;

    String status;

    Long totalPrice;

    Boolean deleted;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    List<OrderItemResponseDTO> orderItems;
}

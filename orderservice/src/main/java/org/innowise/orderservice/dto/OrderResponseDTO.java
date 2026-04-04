package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderResponseDTO {

    Long id;

    Long userId;

    String status;

    String totalPrice;

    Boolean deleted;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    List<OrderItemResponseDTO> orderItems;

    UserResponseDTO user;
}

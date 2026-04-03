package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderItemResponseDTO {

    Long id;

    Long orderId;

    ItemResponseDTO item;

    Long quantity;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}

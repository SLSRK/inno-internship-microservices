package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ItemResponseDTO {

    Long id;

    String name;

    String price;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}

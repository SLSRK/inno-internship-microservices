package org.innowise.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderUpdateDTO {

    Long userId;

    String status;

    List<OrderItemRequestDTO> orderItems;
}

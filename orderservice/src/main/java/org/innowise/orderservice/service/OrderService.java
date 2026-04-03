package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO getOrderById(Long id, Long currentUserId, boolean isUser);


    Page<OrderResponseDTO> getAllOrders(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    OrderResponseDTO updateOrderById(Long id, OrderUpdateDTO orderUpdateDTO);

    OrderResponseDTO deleteOrderById(Long id);

}

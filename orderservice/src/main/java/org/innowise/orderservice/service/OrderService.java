package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    /**
     * Create a new order;
     *
     * @param orderRequestDTO a data of a new order;
     * @return returns full data of the created order.
     */
    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    /**
     * Get an order by id of it exists;
     *
     * @param id the id of an order to get;
     * @param currentUserId auth user id;
     * @param isUser auth user role;
     * @return returns full data of the requested order.
     */
    OrderResponseDTO getOrderById(Long id, Long currentUserId, boolean isUser);

    /**
     * Retrieves a paginated list of orders with optional filtering;
     *
     * @param statuses optional status to filter results;
     * @param from optional minimum order date to filter results;
     * @param to optional maximum order date to filter results;
     * @param page optional flag to filter orders by active status;
     * @param size the number of records per page (must be > 0);
     * @return the orders, that match the given criteria.
     */
    Page<OrderResponseDTO> getAllOrders(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );

    /**
     * Get orders by customer's account id;
     *
     * @param userId customer's account id;
     * @return returns the list of customer's orders.
     */
    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    /**
     * Get orders by customer's account e-mail;
     *
     * @param email customer's account e-mail;
     * @return returns the list of customer's orders.
     */
    List<OrderResponseDTO> getOrdersByUserEmail(String email);

    /**
     * Update order's customer, items, quantity, or status by id;
     *
     * @param id the id of an order to update;
     * @param orderUpdateDTO a new data for the order;
     * @return returns full updated data of the order.
     */
    OrderResponseDTO updateOrderById(Long id, OrderUpdateDTO orderUpdateDTO);

    /**
     * Soft delete order by id;
     *
     * @param id an id of an order to delete;
     * @return returns full data of the deleted order.
     */
    OrderResponseDTO deleteOrderById(Long id);

    void setStatus(Long orderId, OrderStatus status);
}

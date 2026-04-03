package org.innowise.orderservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.exception.AccessDeniedException;
import org.innowise.orderservice.exception.NotFoundException;
import org.innowise.orderservice.mapper.OrderItemMapper;
import org.innowise.orderservice.mapper.OrderMapper;
import org.innowise.orderservice.model.Item;
import org.innowise.orderservice.model.Order;
import org.innowise.orderservice.model.OrderItem;
import org.innowise.orderservice.model.OrderStatus;
import org.innowise.orderservice.repository.ItemRepository;
import org.innowise.orderservice.repository.OrderItemRepository;
import org.innowise.orderservice.repository.OrderRepository;
import org.innowise.orderservice.service.OrderService;
import org.innowise.orderservice.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {

        Order order = orderMapper.toEntity(orderRequestDTO);
        order.setStatus(OrderStatus.STATUS_PENDING);

        List<OrderItem> orderItems = orderRequestDTO.getOrderItems().stream()
                .map(dto -> {
                    OrderItem entity = orderItemMapper.toEntity(dto);
                    entity.setOrder(order);

                    Item item = itemRepository.findById(dto.getItemId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Item with id: " + dto.getItemId() + " not found"));
                    entity.setItem(item);

                    return entity;
                })
                .toList();

        order.setTotalPrice(orderItems.stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getQuantity())
                .sum());
        order.setDeleted(false);
        Order savedOrder = orderRepository.save(order);

        orderItemRepository.saveAll(orderItems);

        savedOrder.setOrderItems(orderItems);
        return orderMapper.toDTO(savedOrder);
    }

    public OrderResponseDTO getOrderById(Long id, Long currentUserId, boolean isUser){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if(isUser && currentUserId != order.getUserId()) throw  new AccessDeniedException("Access denied");

        return orderMapper.toDTO(order);
    }

    public Page<OrderResponseDTO> getAllOrders(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Order> spec = Specification
                .where(OrderSpecification.notDeleted())
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toDTO);
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId){
        return orderRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrderById(Long id, OrderUpdateDTO orderUpdateDTO){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setUserId(orderUpdateDTO.getUserId());
        order.setStatus(OrderStatus.valueOf(orderUpdateDTO.getStatus()));

        order.getOrderItems().clear();
        List<OrderItem> newItems = orderUpdateDTO.getOrderItems().stream()
                .map(dto -> {
                    OrderItem entity = orderItemMapper.toEntity(dto);
                    entity.setOrder(order);

                    Item item = itemRepository.findById(dto.getItemId())
                            .orElseThrow(() -> new NotFoundException("Item not found"));
                    entity.setItem(item);

                    return entity;
                })
                .toList();

        order.getOrderItems().addAll(newItems);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    public OrderResponseDTO deleteOrderById(Long id){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        order.setDeleted(true);

        return orderMapper.toDTO(orderRepository.save(order));
    }
}

package org.innowise.orderservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.dto.UserResponseDTO;
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
import org.innowise.orderservice.service.UserService;
import org.innowise.orderservice.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    private final UserService userService;

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
                .collect(Collectors.toCollection(ArrayList::new));

        order.setTotalPrice(orderItems.stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getQuantity())
                .sum());
        order.setDeleted(false);
        Order savedOrder = orderRepository.save(order);

        orderItemRepository.saveAll(orderItems);

        savedOrder.setOrderItems(orderItems);
        OrderResponseDTO savedOrderDTO = orderMapper.toDTO(savedOrder);
        savedOrderDTO.setUser(userService.getUserById(order.getUserId()));
        return savedOrderDTO;
    }

    public OrderResponseDTO getOrderById(Long id, Long currentUserId, boolean isUser){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if(isUser && currentUserId != order.getUserId()) throw  new AccessDeniedException("Access denied");

        OrderResponseDTO orderDTO = orderMapper.toDTO(order);
        orderDTO.setUser(userService.getUserById(order.getUserId()));
        return orderDTO;
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
                .map(order -> {
                    OrderResponseDTO dto = orderMapper.toDTO(order);
                    dto.setUser(userService.getUserById(order.getUserId()));
                    return dto;
                });
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId){
        List <OrderResponseDTO> ordersDTO = orderRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();
        if(ordersDTO.isEmpty()){
            throw new NotFoundException("No orders found");
        }
        ordersDTO.getLast().setUser(userService.getUserById(userId));
        return ordersDTO;
    }

    public List<OrderResponseDTO> getOrdersByUserEmail(String email){
        UserResponseDTO user = userService.getUserByEmail(email);
        List <OrderResponseDTO> ordersDTO = orderRepository.findByUserIdAndDeletedFalse(user.getId())
                .stream()
                .map(orderMapper::toDTO)
                .toList();
        if(ordersDTO.isEmpty()){
            throw new NotFoundException("No orders found");
        }
        ordersDTO.getLast().setUser(user);
        return ordersDTO;
    }

    @Transactional
    public OrderResponseDTO updateOrderById(Long id, OrderUpdateDTO orderUpdateDTO){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));


        order.setUserId(orderUpdateDTO.getUserId());
        order.setStatus(OrderStatus.valueOf(orderUpdateDTO.getStatus()));

        order.getOrderItems().clear();
        orderUpdateDTO.getOrderItems().forEach(dto -> {
            OrderItem entity = orderItemMapper.toEntity(dto);
            entity.setOrder(order);

            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new NotFoundException("Item not found"));
            entity.setItem(item);

            order.getOrderItems().add(entity);
        });

        order.setTotalPrice(order.getOrderItems().stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getQuantity())
                .sum());

        Order savedOrder = orderRepository.save(order);
        OrderResponseDTO orderDTO = orderMapper.toDTO(savedOrder);
        orderDTO.setUser(userService.getUserById(order.getUserId()));
        return orderDTO;
    }

    public OrderResponseDTO deleteOrderById(Long id){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setDeleted(true);

        return orderMapper.toDTO(orderRepository.save(order));
    }
}

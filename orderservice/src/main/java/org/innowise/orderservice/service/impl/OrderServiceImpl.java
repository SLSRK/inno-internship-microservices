package org.innowise.orderservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.OrderItemRequestDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Order order = orderMapper.toEntity(orderRequestDTO);

        UserResponseDTO userResponseDTO = userService.getUserById(order.getUserId());
        if( "Unknown".equals(userResponseDTO.getName()) && userResponseDTO.getSurname() == null){
            throw new NotFoundException("User not found");
        }

        OrderResponseDTO savedOrderDTO = createOrderTransaction(order, orderRequestDTO);
        savedOrderDTO.setUser(userResponseDTO);
        return savedOrderDTO;
    }

    @Transactional
    private OrderResponseDTO createOrderTransaction(Order order, OrderRequestDTO orderRequestDTO){
        order.setStatus(OrderStatus.STATUS_PENDING);

        List<OrderItem> orderItems = mapOrderItems(orderRequestDTO.getOrderItems(),order);
        order.setTotalPrice(calcTotalPrice(orderItems));
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

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponseDTO> orders = orderPage.getContent().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        List<UserResponseDTO> users = userService.getUsersByIds(orders.stream()
                .map(order -> order.getUserId()).toList());

        Map<Long, UserResponseDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserResponseDTO::getId,
                        u -> u,
                        (existing, duplicate) -> existing));

        orders.forEach(o -> o.setUser(
                userMap.getOrDefault(
                        o.getUserId(),
                        UserResponseDTO.builder()
                                .id(o.getUserId())
                                .name("Unknown")
                                .build()
                )
        ));

        return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
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

    public OrderResponseDTO updateOrderById(Long id, OrderUpdateDTO orderUpdateDTO){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        UserResponseDTO userResponseDTO = userService.getUserById(orderUpdateDTO.getUserId());
        if("Unknown".equals(userResponseDTO.getName()) && userResponseDTO.getSurname() == null){
            throw new NotFoundException("User not found");
        }

        OrderResponseDTO orderDTO = updateOrderTransaction(order, orderUpdateDTO);
        orderDTO.setUser(userResponseDTO);
        return orderDTO;
    }

    @Transactional
    private OrderResponseDTO updateOrderTransaction(Order order, OrderUpdateDTO orderUpdateDTO){
        order.setUserId(orderUpdateDTO.getUserId());
        order.setStatus(OrderStatus.valueOf(orderUpdateDTO.getStatus()));

        List<OrderItem> orderItems = mapOrderItems(orderUpdateDTO.getOrderItems(), order);

        order.getOrderItems().clear();
        order.getOrderItems().addAll(orderItems);
        order.setTotalPrice(calcTotalPrice(order.getOrderItems()));

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO deleteOrderById(Long id){
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setDeleted(true);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    private List<OrderItem> mapOrderItems(List<OrderItemRequestDTO> dtos, Order order) {
        return dtos.stream()
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
    }

    private Long calcTotalPrice(List<OrderItem> orderItems){
        return orderItems.stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
    }
}

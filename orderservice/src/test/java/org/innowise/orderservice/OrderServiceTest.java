package org.innowise.orderservice;

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
import org.innowise.orderservice.service.UserService;
import org.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderResponseDTO orderDTO;
    private Item item;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        orderDTO = new OrderResponseDTO(
                1L, 10L, "STATUS_PENDING", "100", false,
                LocalDateTime.now(), LocalDateTime.now(),
                new ArrayList<>(), null
        );

        item = new Item();
        item.setId(1L);
        item.setPrice(100L);
    }

    @Test
    void createOrder_success() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        OrderRequestDTO request = new OrderRequestDTO(10L, List.of(itemDTO));

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(2L);
        Item item = new Item();
        item.setId(1L);
        item.setPrice(100L);

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderItemMapper.toEntity(itemDTO)).thenAnswer(inv -> {
            OrderItem oi = new OrderItem();
            oi.setQuantity(2L);

            Item i = new Item();
            i.setId(1L);
            i.setPrice(100L);

            oi.setItem(i);
            return oi;
        });
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);
        when(userService.getUserById(10L)).thenReturn(mock(UserResponseDTO.class));

        OrderResponseDTO result = orderService.createOrder(request);

        assertNotNull(result);
        verify(orderRepository).save(order);
        verify(orderItemRepository).saveAll(any());
    }

    @Test
    void getOrderById_success() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);
        when(userService.getUserById(10L)).thenReturn(mock(UserResponseDTO.class));

        OrderResponseDTO result = orderService.getOrderById(1L, 10L, true);

        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_accessDenied() {
        order.setUserId(99L);

        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
                () -> orderService.getOrderById(1L, 10L, true));
    }

    @Test
    void getAllOrders_success() {
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll((Specification<Order>) any(), any(Pageable.class))).thenReturn(page);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);
        when(userService.getUserById(any())).thenReturn(mock(UserResponseDTO.class));

        Page<OrderResponseDTO> result = orderService.getAllOrders(
                List.of(OrderStatus.STATUS_PENDING),
                null, null, 0, 10
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrdersByUserId_success() {
        when(orderRepository.findByUserIdAndDeletedFalse(10L))
                .thenReturn(List.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);
        when(userService.getUserById(10L)).thenReturn(mock(UserResponseDTO.class));

        List<OrderResponseDTO> result = orderService.getOrdersByUserId(10L);

        assertFalse(result.isEmpty());
    }

    @Test
    void getOrdersByUserId_notFound() {
        when(orderRepository.findByUserIdAndDeletedFalse(10L))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> orderService.getOrdersByUserId(10L));
    }

    @Test
    void getOrdersByUserEmail_success() {
        UserResponseDTO user = mock(UserResponseDTO.class);
        when(user.getId()).thenReturn(10L);

        when(userService.getUserByEmail("test@mail.com")).thenReturn(user);
        when(orderRepository.findByUserIdAndDeletedFalse(10L))
                .thenReturn(List.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        List<OrderResponseDTO> result =
                orderService.getOrdersByUserEmail("test@mail.com");

        assertFalse(result.isEmpty());
    }

    @Test
    void updateOrder_success() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        OrderUpdateDTO updateDTO = new OrderUpdateDTO(
                10L, String.valueOf(OrderStatus.STATUS_PENDING), List.of(itemDTO)
        );

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(2L);
        Item item = new Item();
        item.setId(1L);
        item.setPrice(100L);

        order.setOrderItems(new ArrayList<>());
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(orderItemMapper.toEntity(itemDTO)).thenAnswer(inv -> {
            OrderItem oi = new OrderItem();
            oi.setQuantity(2L);

            Item i = new Item();
            i.setId(1L);
            i.setPrice(100L);

            oi.setItem(i);
            return oi;
        });
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);
        when(userService.getUserById(10L)).thenReturn(mock(UserResponseDTO.class));

        OrderResponseDTO result = orderService.updateOrderById(1L, updateDTO);

        assertNotNull(result);
    }

    @Test
    void deleteOrder_success() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderResponseDTO result = orderService.deleteOrderById(1L);

        assertTrue(order.isDeleted());
        assertNotNull(result);
    }
}

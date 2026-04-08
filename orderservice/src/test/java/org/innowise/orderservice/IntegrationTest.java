package org.innowise.orderservice;

import jakarta.transaction.Transactional;
import org.innowise.orderservice.dto.*;
import org.innowise.orderservice.model.Item;
import org.innowise.orderservice.model.OrderStatus;
import org.innowise.orderservice.repository.ItemRepository;
import org.innowise.orderservice.service.OrderService;
import org.innowise.orderservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class IntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>(
            DockerImageName.parse("wiremock/wiremock:2.35.0"))
            .withExposedPorts(8080);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("user.service.url", () ->
                "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080));
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setupWireMock() {
        configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));

        stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "id": 1,
                  "name": "Mock",
                  "surname": "User",
                  "birthDate": "2001-01-01",
                  "active": true,
                  "email": "mock@mail.com",
                  "createdAt": "2026-01-01T00:00:00",
                  "updatedAt": "2026-01-01T00:00:00"
                }
            """)));
    }

    @Test
    void shouldCreateAndGetOrder() {
        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(100L);
        item = itemRepository.save(item);

        UserResponseDTO user = userService.getUserById(1L);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(null, item.getId(), 2L);
        OrderRequestDTO orderRequest = new OrderRequestDTO(user.getId(), List.of(itemDTO));

        OrderResponseDTO savedOrder = orderService.createOrder(orderRequest);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUserId()).isEqualTo(user.getId());
        assertThat(savedOrder.getOrderItems()).hasSize(1);

        OrderResponseDTO foundOrder = orderService.getOrderById(savedOrder.getId(), user.getId(), true);

        assertThat(foundOrder.getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.getOrderItems()).hasSize(1);

        OrderItemResponseDTO responseItem = foundOrder.getOrderItems().get(0);
        assertThat(responseItem.getQuantity()).isEqualTo(2L);
        assertThat(responseItem.getItem().getId()).isEqualTo(item.getId());
        assertThat(responseItem.getItem().getName()).isEqualTo("Test Item");
        assertThat(responseItem.getItem().getPrice()).isEqualTo("1.00");
    }

    @Test
    void shouldUpdateOrder() {
        Item item = new Item();
        item.setName("Update Item");
        item.setPrice(200L);
        item = itemRepository.save(item);

        UserResponseDTO user = userService.getUserById(1L);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(null, item.getId(), 1L);
        OrderRequestDTO orderRequest = new OrderRequestDTO(user.getId(), List.of(itemDTO));

        OrderResponseDTO savedOrder = orderService.createOrder(orderRequest);

        OrderItemRequestDTO updatedItemDTO = new OrderItemRequestDTO(null, item.getId(), 5L);
        List<OrderItemRequestDTO> updatedItems = new ArrayList<>();
        updatedItems.add(updatedItemDTO);

        OrderUpdateDTO updateDTO = new OrderUpdateDTO(
                user.getId(),
                String.valueOf(OrderStatus.STATUS_PENDING),
                updatedItems
        );

        OrderResponseDTO updatedOrder = orderService.updateOrderById(savedOrder.getId(), updateDTO);

        OrderItemResponseDTO updatedItem = updatedOrder.getOrderItems().get(0);
        assertThat(updatedItem.getQuantity()).isEqualTo(5L);
        assertThat(updatedOrder.getStatus()).isEqualTo(String.valueOf(OrderStatus.STATUS_PENDING));
    }

    @Test
    void shouldDeleteOrder() {
        Item item = new Item();
        item.setName("Delete Item");
        item.setPrice(300L);
        item = itemRepository.save(item);

        UserResponseDTO user = userService.getUserById(1L);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(null, item.getId(), 1L);
        OrderRequestDTO orderRequest = new OrderRequestDTO(user.getId(), List.of(itemDTO));

        OrderResponseDTO savedOrder = orderService.createOrder(orderRequest);

        OrderResponseDTO deletedOrder = orderService.deleteOrderById(savedOrder.getId());

        assertThat(deletedOrder.getDeleted()).isTrue();
    }
}

package org.innowise.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.exception.AccessDeniedException;
import org.innowise.orderservice.model.OrderStatus;
import org.innowise.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO,
                                                        Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUserId.equals(orderRequestDTO.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(orderService.createOrder(orderRequestDTO));
    }

    @PostMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id,
                                                         Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));


        return ResponseEntity.ok(orderService.getOrderById(id, currentUserId, isUser));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(orderService.getAllOrders(statuses, from, to, page, size));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable Long userId,
                                                                    Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PostMapping("/email/{email}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserEmail(@PathVariable String email,
                                                                    Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(orderService.getOrdersByUserEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrderById(
            @PathVariable Long id,
            @RequestBody OrderUpdateDTO orderUpdateDTO,
            Authentication authentication
            ){
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(orderService.updateOrderById(id, orderUpdateDTO));
    }

    @PutMapping("/{id}/delete")
    public ResponseEntity<OrderResponseDTO> deleteOrderById(@PathVariable Long id,
                                                            Authentication authentication){
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(orderService.deleteOrderById(id));
    }
}

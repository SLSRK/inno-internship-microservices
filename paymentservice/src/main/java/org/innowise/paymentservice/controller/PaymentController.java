package org.innowise.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.TotalSumResponseDTO;
import org.innowise.paymentservice.exception.AccessDeniedException;
import org.innowise.paymentservice.model.PaymentStatus;
import org.innowise.paymentservice.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment (@RequestBody PaymentRequestDTO paymentRequestDTO,
                                                             Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUserId.equals(paymentRequestDTO.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(paymentService.createPayment(paymentRequestDTO));
    }

    @PutMapping
    public ResponseEntity<PaymentResponseDTO> pay (@RequestParam String paymentId,
                                                   Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        return ResponseEntity.ok(paymentService.pay(paymentId, currentUserId, isUser));
    }

    @GetMapping("/get")
    public ResponseEntity<List<PaymentResponseDTO>> getPayments (@RequestParam(required = false) Long userId,
                                                                 @RequestParam(required = false)Long orderId,
                                                                 @RequestParam(required = false)PaymentStatus paymentStatus,
                                                                 Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(paymentService.getPayments(userId, orderId, paymentStatus));
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<TotalSumResponseDTO> getTotalSumForUser(@PathVariable Long userId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                                                  Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentService.sumForUser(userId, from, to));
    }

    @GetMapping("/total")
    public ResponseEntity<TotalSumResponseDTO> getTotalSumForAll(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                                                 Authentication authentication){
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentService.sumForAll(from, to));
    }
}

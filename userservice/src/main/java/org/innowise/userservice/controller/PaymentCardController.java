package org.innowise.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.exception.AccessDeniedException;
import org.innowise.userservice.service.PaymentCardService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDTO> createCard(@Valid @RequestBody PaymentCardDTO paymentCardDTO,
                                                     Authentication authentication){
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(paymentCardService.createCard(paymentCardDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getPaymentCardById(@PathVariable Long id,
                                                             Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id, currentUserId, isUser));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAllCards(
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication){

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentCardService.getAllCards(holder, active, page, size));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PaymentCardDTO>> getPaymentCardsByUserId(@PathVariable Long id,
                                                                        Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isUser && !isAdmin && !currentUserId.equals(id)) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> updatePaymentCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardDTO paymentCardDTO,
            Authentication authentication){

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardDTO));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardDTO> activatePaymentCard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean active,
            Authentication authentication){
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        return ResponseEntity.ok(paymentCardService.setActive(id, active, currentUserId, isUser));
    }
}

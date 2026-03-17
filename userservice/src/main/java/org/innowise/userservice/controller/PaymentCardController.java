package org.innowise.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.service.PaymentCardService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDTO> createCard(@Valid @RequestBody PaymentCardDTO paymentCardDTO){
        return ResponseEntity.ok(paymentCardService.createCard(paymentCardDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getPaymentCardById(@PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAllCards(
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(paymentCardService.getAllCards(holder, active, page, size));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PaymentCardDTO>> getPaymentCardsByUserId(@PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> updatePaymentCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardDTO paymentCardDTO){
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardDTO));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardDTO> activatePaymentCard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean active){
        return ResponseEntity.ok(paymentCardService.setActive(id, active));
    }
}

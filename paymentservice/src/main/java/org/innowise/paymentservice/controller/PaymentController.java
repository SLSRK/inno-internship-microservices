package org.innowise.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.TotalSumResponseDTO;
import org.innowise.paymentservice.model.PaymentStatus;
import org.innowise.paymentservice.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment (@RequestBody PaymentRequestDTO paymentRequestDTO){
        return ResponseEntity.ok(paymentService.createPayment(paymentRequestDTO));
    }

    @PutMapping
    public ResponseEntity<PaymentResponseDTO> pay (@RequestParam String paymentId){
        return ResponseEntity.ok(paymentService.pay(paymentId));
    }

    @GetMapping("/get")
    public ResponseEntity<List<PaymentResponseDTO>> getPayments (@RequestParam(required = false) Long userId,
                                                                 @RequestParam(required = false)Long orderId,
                                                                 @RequestParam(required = false)PaymentStatus paymentStatus){
        return ResponseEntity.ok(paymentService.getPayments(userId, orderId, paymentStatus));
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<TotalSumResponseDTO> getTotalSumForUser(@PathVariable Long userId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to){
        return ResponseEntity.ok(paymentService.sumForUser(userId, from, to));
    }

    @GetMapping("/total")
    public ResponseEntity<TotalSumResponseDTO> getTotalSumForAll(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to){
        return ResponseEntity.ok(paymentService.sumForAll(from, to));
    }
}

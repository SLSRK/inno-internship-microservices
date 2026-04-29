package org.innowise.paymentservice.service;

import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.TotalSumResponseDTO;
import org.innowise.paymentservice.model.PaymentStatus;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

    PaymentResponseDTO createPayment(PaymentRequestDTO paymentDTO);

    PaymentResponseDTO pay(String paymentId);

    List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus paymentStatus);

    TotalSumResponseDTO sumForUser(Long userId, Instant from, Instant to);

    TotalSumResponseDTO sumForAll(Instant from, Instant to);
}

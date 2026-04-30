package org.innowise.paymentservice.service;

import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.TotalSumResponseDTO;
import org.innowise.paymentservice.model.PaymentStatus;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

    /**
     * Create a new payment;
     *
     * @param paymentDTO data of a payment to create;
     * @return returns data of the saved payment.
     */
    PaymentResponseDTO createPayment(PaymentRequestDTO paymentDTO);

    /**
     * Pay the fee(payment) by ID;
     *
     * @param paymentId ID of a fee(payment) to pay;
     * @param currentUserId auth user id;
     * @param isUser auth user role;
     * @return returns data of the payment with the result.
     */
    PaymentResponseDTO pay(String paymentId, Long currentUserId, boolean isUser);

    /**
     * Get payments by criteria;
     *
     * @param userId ID of a payment's payer;
     * @param orderId ID of a payment's order;
     * @param paymentStatus status of a payments to get;
     * @return the payments, that match the given criteria.
     */
    List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus paymentStatus);

    /**
     * Get total sum of payments for date range for a user;
     *
     * @param userId ID of a payment's payer;
     * @param from minimum payment date to filter results;
     * @param to maximum payment date to filter results;
     * @return returns total sum of the payments, that match the given criteria.
     */
    TotalSumResponseDTO sumForUser(Long userId, Instant from, Instant to);

    /**
     * Get total sum of payments for date range for all users
     *
     * @param from minimum payment date to filter results;
     * @param to maximum payment date to filter results;
     * @return returns total sum of the payments, that match the given criteria.
     */
    TotalSumResponseDTO sumForAll(Instant from, Instant to);
}

package org.innowise.paymentservice.repository;

import org.innowise.paymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByOrderId(Long orderId);
}

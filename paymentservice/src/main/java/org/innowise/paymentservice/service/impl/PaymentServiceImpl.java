package org.innowise.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.PaymentStatusDTO;
import org.innowise.paymentservice.dto.TotalSumResponseDTO;
import org.innowise.paymentservice.exception.NotFoundException;
import org.innowise.paymentservice.mapper.PaymentMapper;
import org.innowise.paymentservice.model.Payment;
import org.innowise.paymentservice.model.PaymentStatus;
import org.innowise.paymentservice.repository.PaymentRepository;
import org.innowise.paymentservice.service.ExternalApiService;
import org.innowise.paymentservice.service.KafkaService;
import org.innowise.paymentservice.service.PaymentService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    private final MongoTemplate mongoTemplate;

    private final ExternalApiService externalApiService;
    private final KafkaService kafkaService;

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentDTO){
        Payment payment = paymentMapper.toEntity(paymentDTO);

        payment.setStatus(PaymentStatus.STATUS_PROCESSING);
        payment.setTimestamp(Instant.now());

        paymentRepository.save(payment);
        return paymentMapper.toDTO(payment);
    }

    @Transactional
    public PaymentResponseDTO pay(String paymentId){
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if(externalApiService.getRandomNumber() % 2 == 0){
            payment.setStatus(PaymentStatus.STATUS_SUCCESS);
        }
        else {
            payment.setStatus(PaymentStatus.STATUS_FAILED);
        }

        kafkaService.sendPaymentEvent(new PaymentStatusDTO(payment.getOrderId(),
                String.valueOf(payment.getStatus())));

        paymentRepository.save(payment);
        return paymentMapper.toDTO(payment);
    }

    public List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus paymentStatus){
        Query query = new Query();

        if(userId != null){
            query.addCriteria(Criteria.where("user_id").is(userId));
        }
        if(orderId != null){
            query.addCriteria(Criteria.where("order_id").is(orderId));
        }
        if(paymentStatus != null){
            query.addCriteria(Criteria.where("status").is(paymentStatus));
        }

        return mongoTemplate.find(query, Payment.class).stream()
                .map(mt -> paymentMapper.toDTO(mt))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public TotalSumResponseDTO sumForUser(Long userId, Instant from, Instant to){
        return sumPayments(userId, from, to);
    }

    public TotalSumResponseDTO sumForAll(Instant from, Instant to){
        return sumPayments(null, from, to);
    }

    private TotalSumResponseDTO sumPayments(Long userId, Instant from, Instant to) {

        Query query = new Query();
        query.addCriteria(Criteria.where("timestamp").gte(from).lte(to));

        if (userId != null) {
            query.addCriteria(Criteria.where("user_id").is(userId));
        }

        long total = mongoTemplate.find(query, Payment.class).stream()
                .mapToLong(Payment::getPaymentAmount)
                .sum();

        String formatted = String.format(Locale.US, "%.2f", total / 100.0);

        return new TotalSumResponseDTO(formatted);
    }
}

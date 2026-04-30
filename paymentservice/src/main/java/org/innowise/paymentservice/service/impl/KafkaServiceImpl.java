package org.innowise.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.dto.PaymentStatusDTO;
import org.innowise.paymentservice.service.KafkaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, PaymentStatusDTO> kafkaTemplate;

    public void sendPaymentEvent(PaymentStatusDTO message) {
        kafkaTemplate.send("payment-status", message.getOrderId().toString(), message);
    }
}

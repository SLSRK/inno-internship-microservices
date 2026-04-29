package org.innowise.paymentservice.service;

import org.innowise.paymentservice.dto.PaymentStatusDTO;

public interface KafkaService {

    void sendPaymentEvent(PaymentStatusDTO event);
}

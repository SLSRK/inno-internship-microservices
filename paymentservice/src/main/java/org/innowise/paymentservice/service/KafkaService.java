package org.innowise.paymentservice.service;

import org.innowise.paymentservice.dto.PaymentStatusDTO;

public interface KafkaService {

    /**
     * Message broker for sending payment status;
     *
     * @param event data on the payment result;
     */
    void sendPaymentEvent(PaymentStatusDTO event);
}

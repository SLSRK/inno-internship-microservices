package org.innowise.orderservice.service.consumer;

import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.PaymentStatusDTO;
import org.innowise.orderservice.model.OrderStatus;
import org.innowise.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final OrderService orderService;

    /**
     * Get message with payment status;
     *
     * @param message DTO with order ID and payment status.
     */
    @KafkaListener(
            topics = "payment-status",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(PaymentStatusDTO message) {
        if ("STATUS_SUCCESS".equals(message.getStatus())) {
            orderService.setStatus(message.getOrderId(), OrderStatus.STATUS_PAID);
        }
    }
}

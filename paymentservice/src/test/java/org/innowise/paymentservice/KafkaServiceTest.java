package org.innowise.paymentservice;

import org.innowise.paymentservice.dto.PaymentStatusDTO;
import org.innowise.paymentservice.service.impl.KafkaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    @Mock
    private KafkaTemplate<String, PaymentStatusDTO> kafkaTemplate;

    @InjectMocks
    private KafkaServiceImpl kafkaService;

    @Test
    void sendPaymentEvent_success() {
        PaymentStatusDTO dto = new PaymentStatusDTO(1L, "STATUS_SUCCESS");

        kafkaService.sendPaymentEvent(dto);

        verify(kafkaTemplate).send("payment-status", "1", dto);
    }
}
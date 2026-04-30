package org.innowise.paymentservice;

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
import org.innowise.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ExternalApiService externalApiService;

    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPayment_success() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        Payment payment = new Payment();
        PaymentResponseDTO response = new PaymentResponseDTO();

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(response);

        PaymentResponseDTO result = paymentService.createPayment(request);

        assertEquals(response, result);
        assertEquals(PaymentStatus.STATUS_PROCESSING, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void pay_success_evenNumber() {
        String id = "1";
        Payment payment = new Payment();
        payment.setId(id);
        payment.setOrderId(10L);
        payment.setUserId(1L);

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));
        when(externalApiService.getRandomNumber()).thenReturn(2L);

        PaymentResponseDTO dto = new PaymentResponseDTO();
        when(paymentMapper.toDTO(payment)).thenReturn(dto);

        PaymentResponseDTO result = paymentService.pay(id, 1L, true);

        assertEquals(dto, result);
        assertEquals(PaymentStatus.STATUS_SUCCESS, payment.getStatus());
        verify(kafkaService).sendPaymentEvent(any(PaymentStatusDTO.class));
        verify(paymentRepository).save(payment);
    }

    @Test
    void pay_success_oddNumber() {
        String id = "1";
        Payment payment = new Payment();
        payment.setId(id);
        payment.setOrderId(10L);
        payment.setUserId(1L);

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));
        when(externalApiService.getRandomNumber()).thenReturn(3L);

        when(paymentMapper.toDTO(payment)).thenReturn(new PaymentResponseDTO());

        paymentService.pay(id, 1L, true);

        assertEquals(PaymentStatus.STATUS_FAILED, payment.getStatus());
    }

    @Test
    void pay_notFound() {
        when(paymentRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> paymentService.pay("1", 1L, true));
    }

    @Test
    void getPayments_withFilters() {
        Payment payment = new Payment();
        payment.setUserId(1L);
        payment.setOrderId(2L);
        payment.setStatus(PaymentStatus.STATUS_SUCCESS);

        PaymentResponseDTO dto = new PaymentResponseDTO();

        when(mongoTemplate.find(any(), eq(Payment.class)))
                .thenReturn(List.of(payment));

        when(paymentMapper.toDTO(payment)).thenReturn(dto);

        List<PaymentResponseDTO> result =
                paymentService.getPayments(1L, 2L, PaymentStatus.STATUS_SUCCESS);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void sumForUser_success() {
        Payment p1 = new Payment();
        p1.setPaymentAmount(100L);

        Payment p2 = new Payment();
        p2.setPaymentAmount(250L);

        when(mongoTemplate.find(any(), eq(Payment.class)))
                .thenReturn(List.of(p1, p2));

        TotalSumResponseDTO result =
                paymentService.sumForUser(1L, Instant.now().minusSeconds(1000), Instant.now());

        assertEquals("3.50", result.getTotalSum());
    }

    @Test
    void sumForAll_success() {
        Payment p1 = new Payment();
        p1.setPaymentAmount(300L);

        Payment p2 = new Payment();
        p2.setPaymentAmount(200L);

        when(mongoTemplate.find(any(), eq(Payment.class)))
                .thenReturn(List.of(p1, p2));

        TotalSumResponseDTO result =
                paymentService.sumForAll(Instant.now().minusSeconds(1000), Instant.now());

        assertEquals("5.00", result.getTotalSum());
    }
}
package org.innowise.paymentservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.dto.PaymentStatusDTO;
import org.innowise.paymentservice.model.Payment;
import org.innowise.paymentservice.model.PaymentStatus;
import org.innowise.paymentservice.repository.PaymentRepository;
import org.innowise.paymentservice.service.PaymentService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class IntegrationTest {

    private final List<PaymentStatusDTO> messages = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.7.0");

    static WireMockServer wireMock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri",
                () -> "mongodb://" + mongo.getHost() + ":" + mongo.getMappedPort(27017) + "/testdb");
        registry.add("spring.data.mongodb.host", mongo::getHost);
        registry.add("spring.data.mongodb.port", () -> mongo.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "testdb");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("external.api.url", () -> "http://localhost:8089/random");
        registry.add("jwt.secret", () -> "auth-secret-key-for-json-web-token-service-123456");
    }

    @BeforeAll
    static void startWiremock() {
        wireMock = new WireMockServer(8089);
        wireMock.start();
    }

    @AfterAll
    static void stopWiremock() {
        wireMock.stop();
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private Consumer<String, String> kafkaConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);

        return new KafkaConsumer<>(props);
    }

    @Test
    void createPayment_shouldSaveToMongo_andReturnSaved() {

        wireMock.stubFor(get(urlEqualTo("/random"))
                .willReturn(okJson("[2]")));

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setUserId(1L);
        request.setOrderId(99L);
        request.setPaymentAmount(520L);

        PaymentResponseDTO response = paymentService.createPayment(request);

        Payment savedPayment = paymentRepository.findById(response.getId()).orElseThrow();

        assertEquals(PaymentStatus.STATUS_PROCESSING, savedPayment.getStatus());
    }

    @Test
    void pay_shouldUpdateStatus_andSendKafkaEvent() {

        wireMock.stubFor(get(urlEqualTo("/random"))
                .willReturn(okJson("[2]")));

        Payment payment = new Payment();
        payment.setUserId(1L);
        payment.setOrderId(20L);
        payment.setPaymentAmount(1000L);
        payment.setStatus(PaymentStatus.STATUS_PROCESSING);
        payment.setTimestamp(java.time.Instant.now());

        payment = paymentRepository.save(payment);

        Consumer<String, String> consumer = kafkaConsumer();
        consumer.subscribe(List.of("payment-status"));

        paymentService.pay(payment.getId(), 1L, true);

        var records = consumer.poll(Duration.ofSeconds(5));

        assertFalse(records.isEmpty());
        assertEquals(payment.getOrderId().toString(),
                records.iterator().next().key());

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();

        assertEquals(PaymentStatus.STATUS_SUCCESS, updated.getStatus());

        paymentService.pay(payment.getId(), 1L, true);

        assertEquals(PaymentStatus.STATUS_SUCCESS,
                paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
    }

    @Test
    void getPayments_shouldReturnFiltered() {

        Payment payment = new Payment();
        payment.setUserId(5L);
        payment.setOrderId(50L);
        payment.setPaymentAmount(500L);
        payment.setStatus(PaymentStatus.STATUS_PROCESSING);
        payment.setTimestamp(java.time.Instant.now());

        paymentRepository.save(payment);

        var result = paymentService.getPayments(5L, null, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void sum_shouldCalculateTotal() {

        Payment p1 = new Payment();
        p1.setUserId(7L);
        p1.setPaymentAmount(1000L);
        p1.setTimestamp(java.time.Instant.now());

        Payment p2 = new Payment();
        p2.setUserId(7L);
        p2.setPaymentAmount(2000L);
        p2.setTimestamp(java.time.Instant.now());

        paymentRepository.save(p1);
        paymentRepository.save(p2);

        var sum = paymentService.sumForUser(7L,
                java.time.Instant.now().minusSeconds(3600),
                java.time.Instant.now().plusSeconds(3600));

        assertEquals("30.00", sum.getTotalSum());
    }

    @Test
    void sumAll_shouldWork() {

        Payment payment = new Payment();
        payment.setUserId(9L);
        payment.setPaymentAmount(1500L);
        payment.setTimestamp(java.time.Instant.now());

        paymentRepository.save(payment);

        var sum = paymentService.sumForAll(
                java.time.Instant.now().minusSeconds(3600),
                java.time.Instant.now().plusSeconds(3600));

        assertNotNull(sum.getTotalSum());
    }

}
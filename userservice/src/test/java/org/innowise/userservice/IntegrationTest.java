package org.innowise.userservice;

import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.dto.UserDTO;
import org.innowise.userservice.service.PaymentCardService;
import org.innowise.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentCardService paymentCardService;

    @Test
    void shouldCreateAndGetUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ivan");
        userDTO.setSurname("Slesarenko");
        userDTO.setEmail("ivan@anymail.com");
        userDTO.setBirthDate(LocalDate.of(2001, 6, 25));
        userDTO.setActive(true);
        userDTO.setCards(new ArrayList<>());

        UserDTO saved = userService.createUser(userDTO);

        assertThat(saved.getId()).isNotNull();

        UserDTO foundUser = userService.getUserById(saved.getId(), false);

        assertThat(foundUser.getName()).isEqualTo("Ivan");
        assertThat(foundUser.getSurname()).isEqualTo("Slesarenko");
        assertThat(foundUser.getEmail()).isEqualTo("ivan@anymail.com");
        assertThat(foundUser.getBirthDate()).isEqualTo(LocalDate.of(2001, 6, 25));
    }

    @Test
    void shouldCreateAndGetPaymentCard() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Ivan");
        userDTO.setSurname("Slesarenko");
        userDTO.setEmail("vano@anymail.com");
        userDTO.setBirthDate(LocalDate.of(2001, 6, 25));
        userDTO.setActive(true);
        userDTO.setCards(new ArrayList<>());

        UserDTO savedUser = userService.createUser(userDTO);

        PaymentCardDTO paymentCardDTO = new PaymentCardDTO();
        paymentCardDTO.setNumber("1111999922220000");
        paymentCardDTO.setHolder("Ivan Slesarenko");
        paymentCardDTO.setExpirationDate(LocalDate.of(2028, 6, 25));
        paymentCardDTO.setActive(true);
        paymentCardDTO.setUserId(savedUser.getId());

        PaymentCardDTO savedCard = paymentCardService.createCard(paymentCardDTO);

        assertThat(savedCard.getId()).isNotNull();

        PaymentCardDTO foundCard = paymentCardService.getPaymentCardById(savedCard.getId(), savedUser.getId(), false);

        assertThat(foundCard.getNumber()).isEqualTo("1111999922220000");
        assertThat(foundCard.getHolder()).isEqualTo("Ivan Slesarenko");
        assertThat(foundCard.getExpirationDate()).isEqualTo(LocalDate.of(2028, 6, 25));
        assertThat(foundCard.getUserId()).isEqualTo(savedUser.getId());
    }
}

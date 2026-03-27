package org.innowise.authservice;

import org.innowise.authservice.dto.AuthResponseDTO;
import org.innowise.authservice.dto.LoginRequestDTO;
import org.innowise.authservice.dto.RegisterRequestDTO;
import org.innowise.authservice.dto.ValidateResponseDTO;
import org.innowise.authservice.repository.AuthUserRepository;
import org.innowise.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Java6Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class IntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("auth_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthUserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUser() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "test@mail.com",
                "password",
                1L,
                "ROLE_USER"
        );

        authService.register(request);

        var saved = repository.findByLogin("test@mail.com").orElse(null);

        assertThat(saved).isNotNull();
        assertThat(saved.getLogin()).isEqualTo("test@mail.com");
        assertThat(passwordEncoder.matches("password", saved.getPassword())).isTrue();
        assertThat(saved.getRole().name()).isEqualTo("ROLE_USER");
        assertThat(saved.getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldLoginAndReturnTokens() {
        RegisterRequestDTO register = new RegisterRequestDTO(
                "login@mail.com",
                "password",
                2L,
                "ROLE_USER"
        );

        authService.register(register);

        LoginRequestDTO login = new LoginRequestDTO(
                "login@mail.com",
                "password"
        );

        AuthResponseDTO response = authService.login(login);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void shouldValidateToken() {
        RegisterRequestDTO register = new RegisterRequestDTO(
                "val@mail.com",
                "password",
                3L,
                "ROLE_USER"
        );

        authService.register(register);

        AuthResponseDTO tokens = authService.login(
                new LoginRequestDTO("val@mail.com", "password")
        );

        ValidateResponseDTO valid = authService.validate(tokens.getAccessToken());

        assertThat(valid.getIsValid()).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        ValidateResponseDTO valid = authService.validate("invalid.token.here");

        assertThat(valid.getIsValid()).isFalse();
    }

    @Test
    void shouldRefreshToken() {
        RegisterRequestDTO register = new RegisterRequestDTO(
                "refresh@mail.com",
                "password",
                4L,
                "ROLE_USER"
        );

        authService.register(register);

        AuthResponseDTO tokens = authService.login(
                new LoginRequestDTO("refresh@mail.com", "password")
        );

        AuthResponseDTO refreshed = authService.refresh(tokens.getRefreshToken());

        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isEqualTo(tokens.getRefreshToken());
    }

}

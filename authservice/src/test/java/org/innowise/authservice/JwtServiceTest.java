package org.innowise.authservice;

import org.innowise.authservice.model.Role;
import org.innowise.authservice.service.JwtService;
import org.innowise.authservice.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtServiceTest {

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
    }

    @Test
    void shouldCreateAndValidateAccessToken() {
        String token = jwtService.createAccessToken(1L, Role.ROLE_USER);

        assertThat(token).isNotBlank();

        var claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void shouldCreateAndValidateRefreshToken() {
        String token = jwtService.createRefreshToken(5L);

        assertThat(token).isNotBlank();

        var claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("5");
    }

    @Test
    void shouldExtractUserId() {
        String token = jwtService.createAccessToken(42L, Role.ROLE_ADMIN);

        Long userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void shouldFailOnInvalidToken() {
        assertThatThrownBy(() ->
                jwtService.validateToken("invalid.token")
        ).isInstanceOf(Exception.class);
    }

}

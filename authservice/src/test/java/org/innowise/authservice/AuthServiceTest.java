package org.innowise.authservice;

import org.innowise.authservice.dto.AuthResponseDTO;
import org.innowise.authservice.dto.LoginRequestDTO;
import org.innowise.authservice.dto.RegisterRequestDTO;
import org.innowise.authservice.dto.UserDTO;
import org.innowise.authservice.exception.PasswordException;
import org.innowise.authservice.model.AuthUser;
import org.innowise.authservice.model.Role;
import org.innowise.authservice.repository.AuthUserRepository;
import org.innowise.authservice.service.JwtService;
import org.innowise.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    @Mock
    private AuthUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                repository,
                passwordEncoder,
                jwtService,
                restTemplate
        );
        ReflectionTestUtils.setField(authService, "userServiceUrl", "http://mock");
    }

    @Test
    void register_shouldHashPasswordAndSaveUser() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "test@mail.com",
                "password",
                1L,
                "ROLE_USER",
                new UserDTO()
        );

        when(passwordEncoder.encode("password")).thenReturn("hashed_pass");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(UserDTO.class),
                any(Object[].class)
        )).thenReturn(userDTO);

        ReflectionTestUtils.setField(authService, "userServiceUrl", "http://localhost");
        authService.register(request);

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(repository).save(captor.capture());

        AuthUser saved = captor.getValue();

        assertThat(saved.getLogin()).isEqualTo("test@mail.com");
        assertThat(saved.getPassword()).isEqualTo("hashed_pass");
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(saved.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_shouldReturnTokens_whenPasswordCorrect() {
        AuthUser user = new AuthUser();
        user.setLogin("test@mail.com");
        user.setPassword("hashed");
        user.setRole(Role.ROLE_USER);
        user.setUserId(1L);

        when(repository.findByLogin("test@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.createAccessToken(1L, Role.ROLE_USER)).thenReturn("access");
        when(jwtService.createRefreshToken(1L)).thenReturn("refresh");

        AuthResponseDTO response = authService.login(
                new LoginRequestDTO("test@mail.com", "password")
        );

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        AuthUser user = new AuthUser();
        user.setLogin("test@mail.com");
        user.setPassword("hashed");

        when(repository.findByLogin("test@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(new LoginRequestDTO("test@mail.com", "wrong"))
        ).isInstanceOf(PasswordException.class);
    }

    @Test
    void refresh_shouldReturnNewAccessToken() {
        AuthUser user = new AuthUser();
        user.setUserId(1L);
        user.setRole(Role.ROLE_ADMIN);

        when(jwtService.extractUserId("refreshToken")).thenReturn(1L);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(jwtService.createAccessToken(1L, Role.ROLE_ADMIN)).thenReturn("newAccess");

        AuthResponseDTO response = authService.refresh("refreshToken");

        assertThat(response.getAccessToken()).isEqualTo("newAccess");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    void validate_shouldReturnTrue_whenTokenValid() {
        when(jwtService.validateToken("valid")).thenReturn(mock(io.jsonwebtoken.Claims.class));

        //ValidateResponseDTO result = authService.validate("valid");
        Boolean result = authService.validate("valid");

        //assertThat(result.getIsValid()).isTrue();
        assertTrue(result);
    }

    @Test
    void validate_shouldReturnFalse_whenException() {
        when(jwtService.validateToken("bad")).thenThrow(new RuntimeException());

        //ValidateResponseDTO result = authService.validate("bad");
        Boolean result = authService.validate("bad");

        assertFalse(result);
    }
}

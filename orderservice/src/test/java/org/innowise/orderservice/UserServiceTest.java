package org.innowise.orderservice;

import org.innowise.orderservice.dto.UserResponseDTO;
import org.innowise.orderservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(userService,
                "userServiceUrl", "http://userservice:8080");
    }

    @Test
    void getUserById_success() {
        UserResponseDTO user = mock(UserResponseDTO.class);

        when(restTemplate.getForObject(
                anyString(),
                eq(UserResponseDTO.class),
                eq(1L)
        )).thenReturn(user);

        UserResponseDTO result = userService.getUserById(1L);

        assertNotNull(result);
    }

    @Test
    void getUserByEmail_success() {
        UserResponseDTO user = mock(UserResponseDTO.class);

        when(restTemplate.getForObject(
                anyString(),
                eq(UserResponseDTO.class),
                eq("test@mail.com")
        )).thenReturn(user);

        UserResponseDTO result = userService.getUserByEmail("test@mail.com");

        assertNotNull(result);
    }
}

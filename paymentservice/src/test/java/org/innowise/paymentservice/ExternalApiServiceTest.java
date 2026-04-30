package org.innowise.paymentservice;

import org.innowise.paymentservice.exception.ExternalException;
import org.innowise.paymentservice.service.impl.ExternalApiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalApiServiceImpl externalApiService;

    @Test
    void getRandomNumber_success() {
        ReflectionTestUtils.setField(externalApiService, "externalApiUrl", "url");

        when(restTemplate.getForEntity("url", Long[].class))
                .thenReturn(ResponseEntity.ok(new Long[]{5L}));

        Long result = externalApiService.getRandomNumber();

        assertEquals(5L, result);
    }

    @Test
    void getRandomNumber_fail() {
        ReflectionTestUtils.setField(externalApiService, "externalApiUrl", "url");

        when(restTemplate.getForEntity("url", Long[].class))
                .thenReturn(ResponseEntity.ok(new Long[]{}));

        assertThrows(ExternalException.class,
                () -> externalApiService.getRandomNumber());
    }
}
package org.innowise.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.exception.ExternalException;
import org.innowise.paymentservice.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ExternalApiServiceImpl implements ExternalApiService {

    private final RestTemplate restTemplate;

    @Value("${external.api.url}")
    private String externalApiUrl;

    public Long getRandomNumber() {
        ResponseEntity<Long[]> response = restTemplate.getForEntity(externalApiUrl, Long[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().length == 0) {
            throw new ExternalException("External API failed");
        }

        return response.getBody()[0];
    }
}

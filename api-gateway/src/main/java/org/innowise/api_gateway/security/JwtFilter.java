package org.innowise.api_gateway.security;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtFilter {

    private boolean validateToken(String token) {
        WebClient client = WebClient.create("http://localhost:8083");

        return Boolean.TRUE.equals(
                client.post()
                        .uri("/api/auth/validate")
                        .bodyValue(token)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block()
        );
    }
}

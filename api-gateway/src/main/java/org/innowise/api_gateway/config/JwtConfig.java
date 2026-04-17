package org.innowise.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${jwt.secret}") String secret
    ) {
        //SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        //return NimbusReactiveJwtDecoder.withSecretKey(key).build();
        return NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(secret.getBytes(), "HmacSHA384"))
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }
}
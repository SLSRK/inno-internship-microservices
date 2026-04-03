package org.innowise.orderservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.service.JwtService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey = Keys.hmacShaKeyFor("auth-secret-key-for-json-web-token-service-123456".getBytes());

    public Claims validate(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token){
        return Long.valueOf(validate(token).getSubject());
    }

    public String getRole(String token){
        return validate(token).get("role", String.class);
    }
}
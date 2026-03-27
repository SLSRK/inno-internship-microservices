package org.innowise.authservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.innowise.authservice.model.Role;
import org.innowise.authservice.service.JwtService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey = Keys.hmacShaKeyFor("auth-secret-key-for-json-web-token-service-123456".getBytes());

    private final long accessExpiration = 1000 * 60 * 15;
    private final long refreshExpiration = 1000 * 60 * 60 * 24;

    public String createAccessToken(Long userId, Role role){
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name().replace("ROLE_", ""))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token){
        return Long.valueOf(validateToken(token).getSubject());
    }
}

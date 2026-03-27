package org.innowise.authservice.service;

import io.jsonwebtoken.Claims;
import org.innowise.authservice.model.Role;

public interface JwtService {

    /**
     * Creates access token for authentification;
     * @param userId id of the user logged in;
     * @param role the role of the user logged in;
     * @return returns access token.
     */
    String createAccessToken(Long userId, Role role);

    /**
     * Creates refresh token for session expiration;
     * @param userId id of the user logged in;
     * @return returns refresh token.
     */
    String createRefreshToken(Long userId);

    /**
     * Checks token for validity;
     * @param token token of the user logged in;
     * @return returns token is valid or not.
     */
    Claims validateToken(String token);

    /**
     * Extracts user id from refresh token;
     * @param token refresh token;
     * @return returns user id.
     */
    Long extractUserId(String token);
}

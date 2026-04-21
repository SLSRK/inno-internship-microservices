package org.innowise.authservice.service;

import org.innowise.authservice.dto.AuthResponseDTO;
import org.innowise.authservice.dto.LoginRequestDTO;
import org.innowise.authservice.dto.RegisterRequestDTO;
import org.innowise.authservice.dto.ValidateResponseDTO;

public interface AuthService {

    /**
     * Creates and saves user credentials;
     * @param registerRequestDTO data of the user to create.
     */
    void register(RegisterRequestDTO registerRequestDTO);

    /**
     * Logs in if login and password pair is valid;
     * @param loginRequestDTO login and password;
     * @return returns access token and refresh token.
     */
    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);

    /**
     * Updates access token by refresh token;
     * @param refreshToken refresh token;
     * @return returns new access token.
     */
    AuthResponseDTO refresh(String refreshToken);

    /**
     * Checks access token for validity;
     * @param token access token;
     * @return returns access token is valid or not.
     */
    ValidateResponseDTO validate(String token);
}

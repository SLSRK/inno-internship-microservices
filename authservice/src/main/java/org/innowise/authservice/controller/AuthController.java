package org.innowise.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.innowise.authservice.dto.AuthResponseDTO;
import org.innowise.authservice.dto.LoginRequestDTO;
import org.innowise.authservice.dto.RefreshRequestDTO;
import org.innowise.authservice.dto.RegisterRequestDTO;
import org.innowise.authservice.dto.ValidateRequestDTO;
import org.innowise.authservice.dto.ValidateResponseDTO;
import org.innowise.authservice.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequestDTO registerRequestDTO){
        authService.register(registerRequestDTO);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO){
        return authService.login(loginRequestDTO);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody RefreshRequestDTO token){
        return authService.refresh(token.getRefreshToken());
    }

    @PostMapping("/validate")
    public ValidateResponseDTO validate(@RequestBody ValidateRequestDTO token){
        return authService.validate(token.getToken());
    }
}

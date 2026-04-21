package org.innowise.authservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.authservice.dto.AuthResponseDTO;
import org.innowise.authservice.dto.LoginRequestDTO;
import org.innowise.authservice.dto.RegisterRequestDTO;
import org.innowise.authservice.dto.UserDTO;
import org.innowise.authservice.exception.NotFoundException;
import org.innowise.authservice.exception.PasswordException;
import org.innowise.authservice.exception.RegistrationException;
import org.innowise.authservice.model.AuthUser;
import org.innowise.authservice.model.Role;
import org.innowise.authservice.repository.AuthUserRepository;
import org.innowise.authservice.service.AuthService;
import org.innowise.authservice.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final RestTemplate restTemplate;
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Transactional
    public void register(RegisterRequestDTO registerRequestDTO) {
        AuthUser newUser = new AuthUser();

        newUser.setLogin(registerRequestDTO.getLogin());
        newUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        newUser.setRole(Role.valueOf(registerRequestDTO.getRole()));

        UserDTO userDTO = registerRequestDTO.getUserDTO();
        UserDTO usrResponseDTO;

        try {
            String url = userServiceUrl + "/api/users";
            usrResponseDTO = restTemplate.postForObject(url, userDTO, UserDTO.class);

        } catch (Exception e) {
            throw new RegistrationException("Registration failed:" + e);
        }
        newUser.setUserId(usrResponseDTO.getId());
        authUserRepository.save(newUser);
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO){
        AuthUser user = authUserRepository.findByLogin(loginRequestDTO.getLogin())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())){
            throw new PasswordException("Invalid password");
        }

        String accessToken = jwtService.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtService.createRefreshToken(user.getUserId());

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refresh(String refreshToken){
        Long userId = jwtService.extractUserId(refreshToken);

        AuthUser user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String newAccessToken = jwtService.createAccessToken(userId, user.getRole());

        return new AuthResponseDTO(newAccessToken, refreshToken);
    }

    public boolean validate(String token){
        try{
            jwtService.validateToken(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

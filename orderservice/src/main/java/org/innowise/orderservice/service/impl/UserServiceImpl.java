package org.innowise.orderservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.UserResponseDTO;
import org.innowise.orderservice.exception.NotFoundException;
import org.innowise.orderservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;
    private final String userServiceUrl = "http://userservice:8080";

    @CircuitBreaker(name = "userService", fallbackMethod = "fallBackUserEmail")
    public UserResponseDTO getUserByEmail(String email) {
        String url = userServiceUrl + "/api/users/email/{email}";
        return restTemplate.getForObject(url, UserResponseDTO.class, email);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallBackUserId")
    public UserResponseDTO getUserById(Long id) {
        String url = userServiceUrl + "/api/users/{id}";
        return restTemplate.getForObject(url, UserResponseDTO.class, id);
    }

    private UserResponseDTO fallBackUserId(Long id, Throwable ex) {
        return UserResponseDTO.builder()
                .id(id)
                .name("Unknown")
                .build();
    }

    private UserResponseDTO fallBackUserEmail(String email, Throwable ex){
        throw new NotFoundException("Cannot find user with E-mail: " + email + ". Try again later.");
    }
}
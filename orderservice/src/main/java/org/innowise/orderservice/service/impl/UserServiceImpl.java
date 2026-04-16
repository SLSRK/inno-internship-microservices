package org.innowise.orderservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.UserResponseDTO;
import org.innowise.orderservice.exception.NotFoundException;
import org.innowise.orderservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;
    private final String userServiceUrl = "http://userservice:8081";

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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallBackUsersIds")
    public List<UserResponseDTO> getUsersByIds(List<Long> ids){
        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String url = userServiceUrl + "/api/users/batch/{ids}";
        ResponseEntity<UserResponseDTO[]> response =
                restTemplate.getForEntity(url, UserResponseDTO[].class, idsParam);

        return Arrays.asList(response.getBody());
    }

    private UserResponseDTO fallBackUserId(Long id, Throwable ex) {
        return UserResponseDTO.builder()
                .id(id)
                .name("Unknown")
                .build();
    }

    private List<UserResponseDTO> fallBackUsersIds(List<Long> ids, Throwable ex) {
        return ids.stream()
                .map(id -> UserResponseDTO.builder()
                        .id(id)
                        .name("Unknown")
                        .build())
                .toList();
    }

    private UserResponseDTO fallBackUserEmail(String email, Throwable ex){
        throw new NotFoundException("Cannot find user with E-mail: " + email + ". Try again later.");
    }
}
package org.innowise.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.UserResponseDTO;
import org.innowise.orderservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;
    private final String userServiceUrl = "http://userservice:8080";

    public UserResponseDTO getUserByEmail(String email) {
        String url = userServiceUrl + "/api/users/email/{email}";
        return restTemplate.getForObject(url, UserResponseDTO.class, email);
    }

    public UserResponseDTO getUserById(Long id) {
        String url = userServiceUrl + "/api/users/{id}";
        return restTemplate.getForObject(url, UserResponseDTO.class, id);
    }
}
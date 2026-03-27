package org.innowise.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {

    String accessToken;

    String refreshToken;
}

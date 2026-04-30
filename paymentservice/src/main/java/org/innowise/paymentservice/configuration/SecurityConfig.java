package org.innowise.paymentservice.configuration;

import lombok.RequiredArgsConstructor;
import org.innowise.paymentservice.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/public/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter,
                        org.springframework.security
                                .web.authentication
                                .UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
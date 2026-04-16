package org.innowise.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception{
        /*return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
/*                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyExchange()
                        .permitAll())*/
                /*.authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();*/
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/authservice/auth/login",
                                "/authservice/auth/register",

                                "/authservice/v3/api-docs",
                                "/userservice/v3/api-docs",
                                "/orderservice/v3/api-docs",

                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                /*.oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                )*/
                .build();
    }
}
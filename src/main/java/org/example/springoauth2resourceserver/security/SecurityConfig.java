package org.example.springoauth2resourceserver.security;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.config.LocalRoleJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ACTIVE @PREAUTHORIZE SUR LES METHODES
@RequiredArgsConstructor
public class SecurityConfig {

    private final LocalRoleJwtAuthenticationConverter localRoleConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users/register").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        // MODIFICATION ICI : On remplace cognitoJwtAuthenticationConverter par localRoleConverter
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(localRoleConverter))
                );

        return http.build();
    }
}
package org.example.springoauth2resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocalRoleJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
//Class qui ajoute le role dans la jwt pour permettre l acces au endpoint preauthorise
    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extraction du SUB de l'Access Token Cognito
        String sub = jwt.getSubject();

        // Récupération des rôles depuis la base MySQL locale
        Collection<GrantedAuthority> authorities = userRepository.findBySub(sub)
                .map(user -> user.getRoles().stream()
                        // Spring Security exige le préfixe "ROLE_" pour matcher avec hasRole('ADMIN')
                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getRoles().name()))
                        .collect(Collectors.toList()))
                // Liste vide si l'utilisateur n'est pas encore en BDD (ex: pendant l'appel au /register)
                .orElse(Collections.emptyList());

        return new JwtAuthenticationToken(jwt, authorities);
    }
}

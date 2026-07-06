package org.example.springoauth2resourceserver.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
//CALSS POUR CONVERTIR LES ROLES DANS LE DB LISIBLE PAR SPRING POUR LES ENDPOINT SPECIALISER
@Component
public class CognitoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwtSource) {
                List<String> groups = jwtSource.getClaimAsStringList("cognito:groups");

                if (groups == null || groups.isEmpty()) {
                    return Collections.emptyList();
                }

                return groups.stream()
                        .map(group -> new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()))
                        .collect(Collectors.toList());
            }
        });

        return jwtAuthenticationConverter.convert(jwt);
    }
}

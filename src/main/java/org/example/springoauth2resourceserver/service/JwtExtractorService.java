package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.dto.RegistrationContextDTO;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtExtractorService {

    private final JwtDecoder jwtDecoder;
// ce service permet d extraire les information des 2 token
    public RegistrationContextDTO extractRegistrationContext(Jwt accessToken, String idTokenStr) {
        Jwt idToken = jwtDecoder.decode(idTokenStr);

        return RegistrationContextDTO.builder()
                .sub(accessToken.getSubject())
                .email(idToken.getClaimAsString("email"))
                .name(idToken.getClaimAsString("name"))
                .familyName(idToken.getClaimAsString("family_name"))
                .build();
    }
}//Ce service prend les jetons bruts et s'occupe de fabriquer le RegistrationContextDTO

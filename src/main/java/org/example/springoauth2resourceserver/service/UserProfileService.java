package org.example.springoauth2resourceserver.service;

import org.example.springoauth2resourceserver.entity.User_Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserProfileService {
    Map<String, String> updateNickname(String sub, String nickname);

    // La méthode pour récupérer le profil avec le cache Redis
    User_Profile getUserProfileWithCache(String sub);

    String uploadAvatar(Jwt jwt, MultipartFile file) throws Exception;
}

package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#sub")
    public Map<String, String> updateNickname(String sub, String nickname) {
        // 1. Vérifier si le pseudo est déjà pris dans MySQL

        if (userProfileRepository.existsByNickname(nickname)) {
            // On lève une exception que Spring pourra intercepter, ou une RuntimeException basique
            throw new IllegalArgumentException("The nickname '" + nickname + "' is already used.");
        }

        // 2. Récupérer le profil de l'utilisateur connecté
        User_Profile profile = userProfileRepository.findByUserUuid_Sub(sub)
                .orElseThrow(() -> new RuntimeException("USER PROFILE NOT FOUND"));

        // 3. Mettre à jour le pseudo et sauvegarder
        profile.setNickname(nickname);
        userProfileRepository.save(profile);

        return Collections.singletonMap("message", "Nickname '" + nickname + "' has been successfully registered!");
    }

    // Lecture depuis Redis (ou MySQL si absent du cache)
    @Override
    @Cacheable(value = "userProfiles", key = "#sub")
    public User_Profile getUserProfileWithCache(String sub) {
        return userProfileRepository.findByUserUuid_Sub(sub)
                .orElseThrow(() -> new RuntimeException("USER PROFILE NOT FOUND"));
    }
}

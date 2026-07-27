package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    // 1. INJECTION DE S3StorageService
    private final S3StorageService s3StorageService;

    // 2. CONSTANTES POUR LA VALIDATION (1 Mo et format JPG)
    private static final long MAX_FILE_SIZE = 1 * 1024 * 1024; // 1 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpg", "image/jpeg");

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
    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#jwt.claims['sub']")
    public String uploadAvatar(Jwt jwt, MultipartFile file) throws IOException {

        // 1. Contrôles de validation (fichier vide, taille, format JPG)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size of 1 MB.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean isValidMime = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean isValidExtension = originalFilename != null &&
                (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg"));

        if (!isValidMime || !isValidExtension) {
            throw new IllegalArgumentException("Invalid file format. Only JPG/JPEG files are allowed.");
        }

        // 2. Récupération du profil
        String userSub = jwt.getClaimAsString("sub");
        User_Profile profile = userProfileRepository.findByUserUuid_Sub(userSub)
                .orElseThrow(() -> new RuntimeException("USER PROFILE NOT FOUND for sub: " + userSub));

        // Vérifier si l'utilisateur a configuré son pseudonyme
        if (profile.getNickname() == null || profile.getNickname().isBlank()) {
            throw new IllegalStateException("User must set a nickname before uploading an avatar.");
        }

        // 3. Upload vers S3 avec le NICKNAME à la place du SUB
        String originalS3Key = s3StorageService.uploadOriginalAvatar(profile.getNickname(), file);

        // Enregistrement dans la DB
        profile.setOriginalAvatarUrl(originalS3Key);
        userProfileRepository.save(profile);

        log.info("[UserProfileService] Original avatar uploaded to S3: {}", originalS3Key);

        return "Image uploaded successfully. Conversion in progress...";
    }
}

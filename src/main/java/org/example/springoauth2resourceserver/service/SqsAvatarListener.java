package org.example.springoauth2resourceserver.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsAvatarListener {

    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    @SqsListener("${aws.sqs.avatar-queue-name}")
    @Transactional
    public void handleAvatarConverted(String rawJsonMessage) {
        try {
            log.info("[SQS Listener] SIGNAL S3 RECEIVED FROM SQS");

            JsonNode rootNode = objectMapper.readTree(rawJsonMessage);

            // Extraction de la clé S3 depuis l'événement S3 Event Notification SQS
            JsonNode recordsNode = rootNode.path("Records");
            if (recordsNode.isArray() && !recordsNode.isEmpty()) {
                String rawS3Key = recordsNode.get(0).path("s3").path("object").path("key").asText();

                // Décoder la clé S3 (gestion des caractères spéciaux/espaces)
                String s3Key = URLDecoder.decode(rawS3Key, StandardCharsets.UTF_8);

                log.info("[SQS Listener] Clé S3 extrait : {}", s3Key);

                // Traiter uniquement si c'est une image redimensionnée
                if (s3Key.contains("resized-images/")) {

                    // Format attendu : resized-images/l.pazienza/{nickname}/resized-filename.jpg
                    String[] parts = s3Key.split("/");
                    if (parts.length >= 3) {
                        String nickname = parts[2]; // Récupère le nickname dans l'arborescence

                        userProfileRepository.findByNickname(nickname)
                                .ifPresentOrElse(profile -> {
                                    profile.setAvatar_url(s3Key);
                                    userProfileRepository.save(profile);
                                    log.info("[SQS Listener] avatar_url UPDATED FOR {}: {}", nickname, s3Key);
                                }, () -> log.error("[SQS Listener] PROFILE NOT FOUND FOR NICKNAME : {}", nickname));
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SQS Listener] ERROR WHILE PROCESSING SQS MESSAGE", e);
        }
    }
}

package org.example.springoauth2resourceserver.controller;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springoauth2resourceserver.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/nickname")
    public ResponseEntity<Map<String, String>> setupNickname(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String nickname) {

        String sub = jwt.getClaimAsString("sub");

        // Le contrôleur délègue tout le travail au service
        Map<String, String> response = userProfileService.updateNickname(sub, nickname);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        try {
            String message = userProfileService.uploadAvatar(jwt, file);
            return ResponseEntity.ok(Map.of("message", message));

        } catch (IllegalArgumentException e) {
            // Fichier vide, > 1MB ou format pas JPG
            log.warn("[Upload Avatar] Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("[Upload Avatar] Server error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while uploading the file."));
        }
    }
}

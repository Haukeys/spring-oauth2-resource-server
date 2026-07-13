package org.example.springoauth2resourceserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
}

package org.example.springoauth2resourceserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.dto.RegistrationContextDTO;
import org.example.springoauth2resourceserver.dto.UserProfilResponseDTO;
import org.example.springoauth2resourceserver.dto.UserRegistrationResponseDTO;
import org.example.springoauth2resourceserver.entity.Roles;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.mapper.UserMapper;
import org.example.springoauth2resourceserver.service.JwtExtractorService;
import org.example.springoauth2resourceserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtExtractorService jwtExtractorService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDTO> registerUser(
            @AuthenticationPrincipal Jwt accessToken,
            @RequestHeader("X-ID-Token") String idTokenStr
    ) {
        // 1. Extraction du contexte depuis les jetons JWT
        RegistrationContextDTO context = jwtExtractorService.extractRegistrationContext(accessToken, idTokenStr);

        // 2. Traitement complet par la couche service
        UserRegistrationResponseDTO response = userService.signUp(context);

        return ResponseEntity.ok(response);
    }

    // Récupération des informations de l'utilisateur connecté via son Access Token
    @GetMapping("/info")
    public ResponseEntity<UserRegistrationResponseDTO> getUserInfo(@AuthenticationPrincipal Jwt accessToken) {
        String sub = accessToken.getSubject();
        User_Profile profile = userService.getUserInfo(sub);
        return ResponseEntity.ok(userMapper.toResponseDto(profile));
    }

    // Désactivation d'un utilisateur (Réservé ADMIN ou MANAGER)
    @PatchMapping("/{sub}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> disableUser(@PathVariable String sub) {
        userService.disableUser(sub);
        return ResponseEntity.noContent().build();
    }

    // Activation d'un utilisateur (Réservé ADMIN ou MANAGER)
    @PatchMapping("/{sub}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> enableUser(@PathVariable String sub) {
        userService.enableUser(sub);
        return ResponseEntity.noContent().build();
    }

    // Modification du rôle d'un utilisateur (Réservé ADMIN uniquement)
    @PatchMapping("/{sub}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable String sub,
            @RequestHeader("role") Roles role
    ) {
        userService.changeUserRole(sub, role);
        return ResponseEntity.noContent().build();
    }
    //donne le user profile complet de l utilisateur connecter
    // Donne le user profile complet de l'utilisateur connecté avec sa biographie
    @GetMapping("/profile")
    public ResponseEntity<UserProfilResponseDTO> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getClaimAsString("sub");

        UserProfilResponseDTO profileDto = userService.getUserProfileBySub(sub);
        return ResponseEntity.ok(profileDto);
    }

    @PutMapping("/profile/bio")
    public ResponseEntity<UserProfilResponseDTO> updateUserBio(
                                                                @AuthenticationPrincipal Jwt jwt,
                                                                @RequestBody String newBio) {

        String sub = jwt.getClaimAsString("sub");

        UserProfilResponseDTO updatedProfile = userService.updateUserBio(sub, newBio);
        return ResponseEntity.ok(updatedProfile);
    }
}

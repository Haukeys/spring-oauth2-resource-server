package org.example.springoauth2resourceserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.dto.FollowListResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfilResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfileSummaryDTO;
import org.example.springoauth2resourceserver.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // S'abonner à un utilisateur par son nickname
    @PostMapping("/{nickname}/follow")
    public ResponseEntity<Map<String, String>> followUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String nickname) {

        String currentSub = jwt.getClaimAsString("sub");
        followService.followUser(currentSub, nickname);
        return ResponseEntity.ok(Collections.singletonMap("message", "You are now following " + nickname));
    }

    // Se désabonner d'un utilisateur par son nickname
    @DeleteMapping("/{nickname}/unfollow")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String nickname) {

        String currentSub = jwt.getClaimAsString("sub");
        followService.unfollowUser(currentSub, nickname);
        return ResponseEntity.ok(Collections.singletonMap("message", "You unfollowed " + nickname));
    }

    // Consulter un profil avec ses compteurs d'abonnés/abonnements
    @GetMapping("/{nickname}")
    public ResponseEntity<UserProfilResponseDTO> getProfileByNickname(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String nickname) {
        //Le rôle de la ligne : Récupérer l'identifiant unique (UUID) de l'utilisateur connecté depuis son jeton JWT Oauth2(sub), sans faire planter l'application s'il n'est pas connecté.
        //
        //jwt != null : Évite une erreur NullPointerException si la requête est faite sans token authentifié.
        //
        //jwt.getClaimAsString("sub") : Extrait le "Subject" (l'ID Oauth2 de l'utilisateur) présent dans le token.
        //
        //: null : Assigne la valeur null si aucun jeton n'est transmis.
        String currentSub = (jwt != null) ? jwt.getClaimAsString("sub") : null;
        return ResponseEntity.ok(followService.getProfileByNickname(currentSub, nickname));
    }
    /**
     * Récupérer la liste des abonnés d'un profil (ceux qui le suivent)
     */
    @GetMapping("/{nickname}/followers")
    public ResponseEntity<FollowListResponseDTO> getFollowers(@PathVariable String nickname) {
        return ResponseEntity.ok(followService.getFollowers(nickname));
    }

    /**
     * Récupérer la liste des abonnements d'un profil (ceux qu'il suit)
     */
    @GetMapping("/{nickname}/following")
    public ResponseEntity<FollowListResponseDTO> getFollowing(@PathVariable String nickname) {
        return ResponseEntity.ok(followService.getFollowing(nickname));
    }
}

package org.example.springoauth2resourceserver.controller;

import lombok.RequiredArgsConstructor;

import org.example.springoauth2resourceserver.dto.PostRequest;
import org.example.springoauth2resourceserver.entity.MediaContent;
import org.example.springoauth2resourceserver.entity.Post;
import org.example.springoauth2resourceserver.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

// --- GESTION DES POSTS ---

    @PostMapping
    public ResponseEntity<Post> createPost(@AuthenticationPrincipal Jwt jwt, @RequestBody PostRequest body) {
        String sub = jwt.getClaimAsString("sub"); // On récupère le 'sub' au lieu du 'username'
        return ResponseEntity.ok(postService.createPost(body.getTitle(), body.getContentBlocks(), sub));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable String id, @AuthenticationPrincipal Jwt jwt, @RequestBody PostRequest body) {
        String sub = jwt.getClaimAsString("sub");
        return ResponseEntity.ok(postService.updatePost(id, body.getTitle(), body.getContentBlocks(), sub));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteOwnPost(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getClaimAsString("sub");
        postService.deleteOwnPost(id, sub);
        return ResponseEntity.ok(Collections.singletonMap("message", "Your post has been successfully deleted."));
    }

    @DeleteMapping("/{id}/moderation")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deletePostByModerator(@PathVariable String id) {
        postService.deletePostByModerator(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "The post has been deleted by the moderation team."));
    }

    // --- GESTION DES COMMENTAIRES ---

    @PostMapping("/{id}/comments")
    public ResponseEntity<Post> addComment(@PathVariable String id, @AuthenticationPrincipal Jwt jwt, @RequestBody List<MediaContent> contentBlocks) {
        String sub = jwt.getClaimAsString("sub");
        // Plus besoin de récupérer le 'username' ici, le service s'occupe de chercher le pseudo via le sub
        return ResponseEntity.ok(postService.addComment(id, contentBlocks, sub));
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Post> updateComment(
            @PathVariable String id,
            @PathVariable String commentId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<MediaContent> contentBlocks) {

        String sub = jwt.getClaimAsString("sub");
        return ResponseEntity.ok(postService.updateComment(id, commentId, contentBlocks, sub));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteOwnComment(
            @PathVariable String id,
            @PathVariable String commentId,
            @AuthenticationPrincipal Jwt jwt) {

        String sub = jwt.getClaimAsString("sub");
        postService.deleteOwnComment(id, commentId, sub);
        return ResponseEntity.ok(Collections.singletonMap("message", "Your comment has been successfully deleted."));
    }

    @DeleteMapping("/{id}/comments/{commentId}/moderation")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCommentByModerator(@PathVariable String id, @PathVariable String commentId) {
        postService.deleteCommentByModerator(id, commentId);
        return ResponseEntity.ok(Collections.singletonMap("message", "The comment has been deleted by the moderation team."));
    }
}

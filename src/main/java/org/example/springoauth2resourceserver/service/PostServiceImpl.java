package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;

import org.example.springoauth2resourceserver.entity.Comment;
import org.example.springoauth2resourceserver.entity.MediaContent;
import org.example.springoauth2resourceserver.entity.Post;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.repository.PostRepository;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;

    public Post createPost(String title, List<MediaContent> contentBlocks, String sub) {
        // Chercher le profil de l'utilisateur dans MySQL via son sub
        User_Profile profile = userProfileRepository.findByUserUuid_Sub(sub)
                .orElseThrow(() -> new RuntimeException("USER PROFILE NOT FOUND"));

        // Vérifier si le nickname est vide
        if (profile.getNickname() == null || profile.getNickname().isBlank()) {
            throw new IllegalStateException("A unique nickname is required before you can create a post.");
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContentBlocks(contentBlocks);
        post.setAuthorName(profile.getNickname()); // On utilise le pseudo MySQL unique !
        return postRepository.save(post);
    }

    public Post updatePost(String postId, String newTitle, List<MediaContent> newContentBlocks, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        if (!post.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("you are not the author of this post.");
        }

        post.setTitle(newTitle);
        post.setContentBlocks(newContentBlocks);
        // lastModifiedBy et lastModifiedAt mis à jour automatiquement à la sauvegarde
        return postRepository.save(post);
    }

    public Post addComment(String postId, List<MediaContent> contentBlocks, String sub) { // Enlevé "name"
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        // Chercher le profil de l'utilisateur dans MySQL via son sub
        User_Profile profile = userProfileRepository.findByUserUuid_Sub(sub)
                .orElseThrow(() -> new RuntimeException("USER PROFILE NOT FOUND"));

        // Vérifier si le nickname est vide
        if (profile.getNickname() == null || profile.getNickname().isBlank()) {
            throw new IllegalStateException("A unique nickname is required before you can post a comment.");
        }

        Comment comment = new Comment();
        comment.setContentBlocks(contentBlocks);
        comment.setAuthorName(profile.getNickname()); // On utilise le pseudo MySQL unique !

        comment.setCreatedBy(sub);
        comment.setCreatedAt(LocalDateTime.now());

        post.getComments().add(comment);
        return postRepository.save(post);
    }

    public Post updateComment(String postId, String commentId, List<MediaContent> newContentBlocks, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("COMMENT NOT FOUND"));

        if (!comment.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("you are not hte author of this comment.");
        }

        comment.setContentBlocks(newContentBlocks);
        comment.setLastModifiedBy(sub);
        comment.setLastModifiedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public void deleteOwnPost(String postId, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        if (!post.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("Vous n'êtes pas l'auteur.");
        }
        postRepository.delete(post);
    }

    public Post deleteCommentByModerator(String postId, String commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        post.getComments().removeIf(c -> c.getId().equals(commentId));
        return postRepository.save(post);
    }
    // Supprimer son propre commentaire (Vérification du créateur)
    public Post deleteOwnComment(String postId, String commentId, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("COMMENT NOT FOUND"));

        // Seul l'auteur du commentaire a le droit de le supprimer ici
        if (!comment.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("Vous n'êtes pas l'auteur de ce commentaire.");
        }

        post.getComments().remove(comment);
        return postRepository.save(post);
    }

    // Modération : Supprimer n'importe quel Post (Réservé aux MANAGERS / ADMINS)
    public void deletePostByModerator(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("POST NOT FOUND");
        }
        postRepository.deleteById(postId);
    }
    @Override
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
}
package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;

import org.example.springoauth2resourceserver.dto.PostPageResponse;
import org.example.springoauth2resourceserver.entity.Comment;
import org.example.springoauth2resourceserver.entity.MediaContent;
import org.example.springoauth2resourceserver.entity.Post;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.repository.PostRepository;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;

    // Vide le cache des listes de posts puisqu'un nouveau post vient d'arriver
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public Post createPost(String title, List<MediaContent> contentBlocks, String sub) {
        // Chercher le profil de l'utilisateur dans MySQL via son sub
        User_Profile profile = userProfileService.getUserProfileWithCache(sub);

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

    // Vide le cache car le contenu du post a changé
    @CacheEvict(value = "posts", allEntries = true)
    @Override
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

    // Vide le cache car un nouveau commentaire a été ajouté au post
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public Post addComment(String postId, List<MediaContent> contentBlocks, String sub) {
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

    // Vide le cache car le contenu d'un commentaire a changé
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public Post updateComment(String postId, String commentId, List<MediaContent> newContentBlocks, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("COMMENT NOT FOUND"));

        if (!comment.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("you are not the author of this comment.");
        }

        comment.setContentBlocks(newContentBlocks);
        comment.setLastModifiedBy(sub);
        comment.setLastModifiedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    // Vide le cache car le post est supprimé
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public void deleteOwnPost(String postId, String sub) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        if (!post.getCreatedBy().equals(sub)) {
            throw new AccessDeniedException("Vous n'êtes pas l'auteur.");
        }
        postRepository.delete(post);
    }

    // Vide le cache car un commentaire a été supprimé du post (modération)
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public Post deleteCommentByModerator(String postId, String commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST NOT FOUND"));

        post.getComments().removeIf(c -> c.getId().equals(commentId));
        return postRepository.save(post);
    }

    // Vide le cache car l'utilisateur a supprimé son propre commentaire
    @CacheEvict(value = "posts", allEntries = true)
    @Override
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

    // Vide le cache lors d'une action de modération (suppression de post)
    @CacheEvict(value = "posts", allEntries = true)
    @Override
    public void deletePostByModerator(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("POST NOT FOUND");
        }
        postRepository.deleteById(postId);
    }

    // La méthode principale appelée par le Controller
    // Elle est désormais mise en cache directement (évite le count() MongoDB et le find)
    @Cacheable(value = "posts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Override
    public PostPageResponse getAllPosts(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);

        return PostPageResponse.builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
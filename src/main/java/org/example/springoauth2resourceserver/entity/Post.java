package org.example.springoauth2resourceserver.entity;

import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.example.springoauth2resourceserver.entity.Comment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {

    @Id
    private String id; // Généré automatiquement en String par Spring/MongoDB

    private String authorName;

    private String title;

    // Contenu multi-type pour le post
    private List<MediaContent> contentBlocks = new ArrayList<>();

    // Liste des commentaires imbriqués
    private List<Comment> comments = new ArrayList<>();

    // Auditing Automatique Spring Data Mongo
    @CreatedBy
    private String createdBy; // Contient le 'sub' Cognito du créateur

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedBy
    private String lastModifiedBy; // Le 'sub' du dernier modificateur

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;
}
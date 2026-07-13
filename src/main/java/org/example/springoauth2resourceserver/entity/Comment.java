package org.example.springoauth2resourceserver.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Comment {
    private String id = UUID.randomUUID().toString();
    private String authorName;

    // Contenu multi-type pour le commentaire
    private List<MediaContent> contentBlocks = new ArrayList<>();

    // Auditing manuel spécifique aux objets imbriqués
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;
}

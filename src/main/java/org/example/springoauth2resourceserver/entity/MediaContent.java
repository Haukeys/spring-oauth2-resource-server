package org.example.springoauth2resourceserver.entity;

import lombok.Data;

@Data
public class MediaContent {
    private String type;  // "TEXT", "IMAGE", "GIF", "VIDEO"
    private String value; // Le texte brut OU l'URL du média (ex: stocké sur S3)
    private Integer order; // Pour conserver l'ordre d'affichage exact

}

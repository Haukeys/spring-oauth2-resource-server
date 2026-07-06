package org.example.springoauth2resourceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfilResponseDTO {

    private UUID idProfile;
    private String name;
    private String surname;
    private String avatar_url;
    private String biography;

    //champs d'auditing optionnels si le client doit les afficher
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

}

package org.example.springoauth2resourceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarConvertedEvent {
    private String userSub;
    private String convertedKey; // Exemple: "resized-images/USER/{userSub}/avatar.jpg"
}

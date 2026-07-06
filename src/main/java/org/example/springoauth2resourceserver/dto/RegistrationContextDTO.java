package org.example.springoauth2resourceserver.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationContextDTO {
    private String sub;
    private String email;
    private String name;
    private String familyName;
}//Ce DTO sert uniquement d'enveloppe temporaire pour passer les données extraites des deux JWT à MapStruct.

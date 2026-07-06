package org.example.springoauth2resourceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationResponseDTO {
    private UUID uuid;
    private String sub;
    private String email;
    private String name;
    private String surname;
    private Set<String> roles;
    private Boolean isActive;
}

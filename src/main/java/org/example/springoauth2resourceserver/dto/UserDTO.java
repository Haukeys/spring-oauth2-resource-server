package org.example.springoauth2resourceserver.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.springoauth2resourceserver.entity.Role;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID uuid;

    private String sub;

    private String email;

    private Set<Role> roles;

    private Boolean isActive=true;
}

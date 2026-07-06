package org.example.springoauth2resourceserver.mapper;

import org.example.springoauth2resourceserver.dto.RegistrationContextDTO;
import org.example.springoauth2resourceserver.dto.UserRegistrationResponseDTO;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Conversion du DTO de contexte (JWTs) vers l'entité User_Profile
    @Mapping(target = "idProfile", ignore = true)
    @Mapping(target = "userUuid.sub", source = "sub")
    @Mapping(target = "userUuid.email", source = "email")
    @Mapping(target = "userUuid.isActive", constant = "true")
    @Mapping(target = "surname", source = "familyName")
    @Mapping(target = "avatar_url", ignore = true)
    @Mapping(target = "biography", ignore = true)
    User_Profile toEntity(RegistrationContextDTO context);

    // 2. Conversion de l'entité User_Profile vers le DTO de réponse client
    @Mapping(target = "uuid", source = "userUuid.uuid")
    @Mapping(target = "sub", source = "userUuid.sub")
    @Mapping(target = "email", source = "userUuid.email")
    @Mapping(target = "isActive", source = "userUuid.isActive")
    @Mapping(target = "roles", source = "userUuid.roles", qualifiedByName = "mapRoles")
    UserRegistrationResponseDTO toResponseDto(User_Profile profile);

    /*
     * EXPLICATION DE LA MÉTHODE MAPROLES :
     * * Pourquoi cette méthode et ces annotations sont obligatoires ?
     * * 1. Incompatibilité de type :
     * L'entité utilise une collection d'objets complexes 'Set<Role>' issus de la BDD.
     * Le DTO de réponse attend une simple liste de chaînes 'Set<String>' (ex: ["USER"]).
     * MapStruct ne sait pas deviner tout seul comment extraire un texte depuis un objet Role.
     * * 2. Rôle du code ci-dessous :
     * - La méthode 'mapRoles' prend le 'Set<Role>', extrait le nom textuel de l'Enum
     * de chaque rôle (role.getRoles().name()) et regroupe le tout en 'Set<String>'.
     * - L'annotation @Named("mapRoles") donne un alias unique à cette logique.
     * - Cet alias est appelé plus haut via 'qualifiedByName = "mapRoles"' pour indiquer
     * à MapStruct d'utiliser précisément ce comportement personnalisé pour ce champ.
     */
    @Named("mapRoles")
    default Set<String> mapRoles(Set<org.example.springoauth2resourceserver.entity.Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getRoles().name())
                .collect(Collectors.toSet());
    }
}

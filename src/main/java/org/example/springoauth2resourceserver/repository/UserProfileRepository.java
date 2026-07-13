package org.example.springoauth2resourceserver.repository;

import org.example.springoauth2resourceserver.entity.User;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<User_Profile, UUID> {

    // Permet de récupérer le profil directement à partir de l'UUID technique de l'User
    Optional<User_Profile> findByUserUuid(User userUuid);

    // Permet de trouver le profil en traversant l'entité User jusqu'à son champ 'sub'
    Optional<User_Profile> findByUserUuid_Sub(String sub);

    // Permet de vérifier si un pseudo est déjà pris par quelqu'un d'autre
    boolean existsByNickname(String nickname);

}

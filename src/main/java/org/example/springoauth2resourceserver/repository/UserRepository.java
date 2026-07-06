package org.example.springoauth2resourceserver.repository;

import org.example.springoauth2resourceserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Permet de vérifier si l'utilisateur est déjà inscrit lors du SignUp
    boolean existsBySub(String sub);

    // Permet de récupérer l'utilisateur via son sub Cognito lors des appels API ultérieurs
    Optional<User> findBySub(String sub);
}

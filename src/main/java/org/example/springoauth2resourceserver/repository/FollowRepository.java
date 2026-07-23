package org.example.springoauth2resourceserver.repository;

import org.example.springoauth2resourceserver.entity.Follow;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // Vérifier si A suit B
    boolean existsByFollowerAndFollowing(User_Profile follower, User_Profile following);

    // Récupérer le lien pour le supprimer (Unfollow)
    Optional<Follow> findByFollowerAndFollowing(User_Profile follower, User_Profile following);

    // Compteurs pour les profils
    long countByFollower(User_Profile follower);   // Nombre d'abonnements (qui je follow)
    long countByFollowing(User_Profile following); // Nombre d'abonnés (qui me follow)

    // Récupérer tous les liens où l'utilisateur est celui qui suit (ses abonnements)
    List<Follow> findByFollower(User_Profile follower);

    // Récupérer tous les liens où l'utilisateur est celui qui est suivi (ses abonnés)
    List<Follow> findByFollowing(User_Profile following);
}
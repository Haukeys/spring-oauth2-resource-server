package org.example.springoauth2resourceserver.repository;

import org.example.springoauth2resourceserver.entity.Role;
import org.example.springoauth2resourceserver.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Permet de récupérer l'entité Role à partir de son Enum (ADMIN, MANAGER, USER)
    Optional<Role> findByRoles(Roles roles);
    boolean existsByRoles(Roles roles);
}

package org.example.springoauth2resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.entity.Role;
import org.example.springoauth2resourceserver.entity.Roles;
import org.example.springoauth2resourceserver.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
//Classe qui crée les role a la creation du prmier utilisateur si aucun n existe en DB
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Boucle sur toutes les valeurs de votre énumération Roles (USER, MANAGER, ADMIN)
        for (Roles roleEnum : Roles.values()) {
            if (!roleRepository.existsByRoles(roleEnum)) {
                Role newRole = new Role();
                newRole.setRoles(roleEnum);
                roleRepository.save(newRole);
                System.out.println("Role initialised during starting : " + roleEnum);
            }
        }
    }
}
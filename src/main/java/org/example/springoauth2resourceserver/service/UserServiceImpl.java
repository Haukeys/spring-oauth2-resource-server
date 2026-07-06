package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.dto.RegistrationContextDTO;
import org.example.springoauth2resourceserver.dto.UserRegistrationResponseDTO;
import org.example.springoauth2resourceserver.entity.Role;
import org.example.springoauth2resourceserver.entity.Roles;
import org.example.springoauth2resourceserver.entity.User;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.mapper.UserMapper;
import org.example.springoauth2resourceserver.repository.RoleRepository;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.example.springoauth2resourceserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserRegistrationResponseDTO signUp(RegistrationContextDTO context) {
        // 1. Conversion interne du DTO vers l'entité User_Profile via le Mapper
        User_Profile profile = userMapper.toEntity(context);

        // 2. Vérification de l'existence via le SUB Cognito
        if (userRepository.existsBySub(profile.getUserUuid().getSub())) {
            throw new IllegalStateException("USER ALREADY REGISTER WITH THIS COGNITO SUB.");
        }

        // 3. Récupération du rôle USER ou création automatique s'il n'existe pas en BDD
        Role userRole = roleRepository.findByRoles(Roles.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoles(Roles.USER);
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // 4. Liaison du rôle à l'entité User imbriquée
        profile.getUserUuid().setRoles(roles);

        // 5. Persistance des entités
        User savedUser = userRepository.save(profile.getUserUuid());
        profile.setUserUuid(savedUser);
        User_Profile savedProfile = userProfileRepository.save(profile);

        // 6. Conversion finale vers le DTO de réponse pour le contrôleur
        return userMapper.toResponseDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public User_Profile getUserInfo(String sub) {
        User user = userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("USER NOT FOUND."));
        return userProfileRepository.findByUserUuid(user.getUuid())
                .orElseThrow(() -> new RuntimeException("PROFILE NOT FOUND."));
    }

    @Override
    @Transactional
    public void disableUser(String sub) {
        User user = userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("USER NOT FOUND."));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void enableUser(String sub) {
        User user = userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("USER NOT FOUND."));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(String sub, Roles newRoleEnum) {
        User user = userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("USER NOT FOUND."));

        Role role = roleRepository.findByRoles(newRoleEnum)
                .orElseThrow(() -> new RuntimeException("SPECIFIED ROLE NOT FOUND."));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
    }
}
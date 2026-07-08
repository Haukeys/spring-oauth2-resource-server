package org.example.springoauth2resourceserver.service;

import org.example.springoauth2resourceserver.dto.RegistrationContextDTO;
import org.example.springoauth2resourceserver.dto.UserProfilResponseDTO;
import org.example.springoauth2resourceserver.dto.UserRegistrationResponseDTO;
import org.example.springoauth2resourceserver.entity.Roles;
import org.example.springoauth2resourceserver.entity.User_Profile;

public interface UserService {
    public UserRegistrationResponseDTO signUp(RegistrationContextDTO context);

    User_Profile getUserInfo(String sub);

    void disableUser(String sub);

    void enableUser(String sub);

    void changeUserRole(String sub , Roles newRoleEnum);

    UserProfilResponseDTO getUserProfileBySub(String sub);

    UserProfilResponseDTO updateUserBio(String sub, String newBio);
}

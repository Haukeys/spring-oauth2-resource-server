package org.example.springoauth2resourceserver.service;

import java.util.Map;

public interface UserProfileService {
    Map<String, String> updateNickname(String sub, String nickname);
}

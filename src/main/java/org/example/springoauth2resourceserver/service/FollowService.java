package org.example.springoauth2resourceserver.service;

import org.example.springoauth2resourceserver.dto.FollowListResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfilResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfileSummaryDTO;

import java.util.List;

public interface FollowService {
    void followUser(String followerSub, String targetNickname);
    void unfollowUser(String followerSub, String targetNickname);
    UserProfilResponseDTO getProfileByNickname(String currentSub, String targetNickname);
    FollowListResponseDTO getFollowers(String nickname);
    FollowListResponseDTO getFollowing(String nickname);
}

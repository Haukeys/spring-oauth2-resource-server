package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import org.example.springoauth2resourceserver.dto.FollowListResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfilResponseDTO;
import org.example.springoauth2resourceserver.dto.UserProfileSummaryDTO;
import org.example.springoauth2resourceserver.entity.Follow;
import org.example.springoauth2resourceserver.entity.User_Profile;
import org.example.springoauth2resourceserver.repository.FollowRepository;
import org.example.springoauth2resourceserver.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService{
    private final FollowRepository followRepository;
    private final UserProfileRepository userProfileRepository;

@Override
@Transactional
public void followUser(String followerSub, String targetNickname) {
    User_Profile follower = userProfileRepository.findByUserUuid_Sub(followerSub)
            .orElseThrow(() -> new RuntimeException("FOLLOWER PROFILE NOT FOUND"));

    User_Profile target = userProfileRepository.findByNickname(targetNickname)
            .orElseThrow(() -> new RuntimeException("TARGET PROFILE NOT FOUND"));

    if (follower.getIdProfile().equals(target.getIdProfile())) {
        throw new IllegalStateException("You cannot follow yourself.");
    }

    if (followRepository.existsByFollowerAndFollowing(follower, target)) {
        throw new IllegalStateException("You are already following this user.");
    }

    Follow follow = Follow.builder()
            .follower(follower)
            .following(target)
            .build();

    followRepository.save(follow);
}

@Override
@Transactional
public void unfollowUser(String followerSub, String targetNickname) {
    User_Profile follower = userProfileRepository.findByUserUuid_Sub(followerSub)
            .orElseThrow(() -> new RuntimeException("FOLLOWER PROFILE NOT FOUND"));

    User_Profile target = userProfileRepository.findByNickname(targetNickname)
            .orElseThrow(() -> new RuntimeException("TARGET PROFILE NOT FOUND"));

    Follow follow = followRepository.findByFollowerAndFollowing(follower, target)
            .orElseThrow(() -> new RuntimeException("YOU ARE NOT FOLLOWING THIS USER"));

    followRepository.delete(follow);
}

@Override
@Transactional(readOnly = true)
public UserProfilResponseDTO getProfileByNickname(String currentSub, String targetNickname) {
    User_Profile targetProfile = userProfileRepository.findByNickname(targetNickname)
            .orElseThrow(() -> new RuntimeException("PROFILE NOT FOUND"));

    long followersCount = followRepository.countByFollowing(targetProfile);
    long followingCount = followRepository.countByFollower(targetProfile);

    boolean isFollowedByMe = false;
    if (currentSub != null) {
        // Un simple optional.flatMap(...) ou une vérification classique résout le problème proprement
        isFollowedByMe = userProfileRepository.findByUserUuid_Sub(currentSub)
                .map(currentProfile -> followRepository.existsByFollowerAndFollowing(currentProfile, targetProfile))
                .orElse(false);
    }

    return UserProfilResponseDTO.builder()
            .nickname(targetProfile.getNickname())
            .name(targetProfile.getName())
            .surname(targetProfile.getSurname())
            .avatar_url(targetProfile.getAvatar_url())
            .biography(targetProfile.getBiography())
            .followersCount(followersCount)
            .followingCount(followingCount)
            .followedByMe(isFollowedByMe)
            .build();
}
    @Override
    @Transactional(readOnly = true)
    public FollowListResponseDTO getFollowers(String nickname) {
        User_Profile targetProfile = userProfileRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("PROFILE NOT FOUND"));

        List<Follow> follows = followRepository.findByFollowing(targetProfile);

        if (follows.isEmpty()) {
            return FollowListResponseDTO.builder()
                    .message(nickname + " has no followers yet.")
                    .users(List.of())
                    .build();
        }

        List<UserProfileSummaryDTO> users = follows.stream()
                .map(Follow::getFollower)
                .map(profile -> UserProfileSummaryDTO.builder()
                        .nickname(profile.getNickname())
                        .name(profile.getName())
                        .surname(profile.getSurname())
                        .avatar_url(profile.getAvatar_url())
                        .build())
                .toList();

        return FollowListResponseDTO.builder()
                .message("Followers list for " + nickname)
                .users(users)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FollowListResponseDTO getFollowing(String nickname) {
        User_Profile targetProfile = userProfileRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("PROFILE NOT FOUND"));

        List<Follow> follows = followRepository.findByFollower(targetProfile);

        if (follows.isEmpty()) {
            return FollowListResponseDTO.builder()
                    .message(nickname + " is not following anyone yet.")
                    .users(List.of())
                    .build();
        }

        List<UserProfileSummaryDTO> users = follows.stream()
                .map(Follow::getFollowing)
                .map(profile -> UserProfileSummaryDTO.builder()
                        .nickname(profile.getNickname())
                        .name(profile.getName())
                        .surname(profile.getSurname())
                        .avatar_url(profile.getAvatar_url())
                        .build())
                .toList();

        return FollowListResponseDTO.builder()
                .message(nickname + " is following " + users.size() + " user(s).")
                .users(users)
                .build();
    }
}

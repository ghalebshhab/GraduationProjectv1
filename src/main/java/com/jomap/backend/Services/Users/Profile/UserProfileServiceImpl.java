package com.jomap.backend.Services.Users.Profile;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.Profile.UserProfile;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getMyProfile(String usernameFromToken) {
        User user = userRepository.findByEmail(usernameFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ApiResponse.error("Profile not found");
        }

        return ApiResponse.success("Profile fetched successfully", mapToResponse(user, profile));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return ApiResponse.error("Profile not found");
        }

        return ApiResponse.success("Profile fetched successfully", mapToResponse(user, profile));
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateMyProfile(String usernameFromToken, UpdateUserProfileRequest request) {
        User user = userRepository.findByEmail(usernameFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ApiResponse.error("Profile not found");
        }

        applyUpdates(user, profile, request);

        userRepository.save(user);
        userProfileRepository.save(profile);

        return ApiResponse.success("Profile updated successfully", mapToResponse(user, profile));
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return ApiResponse.error("Profile not found");
        }

        applyUpdates(user, profile, request);

        userRepository.save(user);
        userProfileRepository.save(profile);

        return ApiResponse.success("Profile updated successfully", mapToResponse(user, profile));
    }

    private void applyUpdates(User user, UserProfile profile, UpdateUserProfileRequest request) {
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            user.setUsername(request.getUserName().trim());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl().trim());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        if (request.getCoverImageUrl() != null) {
            profile.setCoverImageUrl(request.getCoverImageUrl().trim());
        }

        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation().trim());
        }

        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
        }

        if (request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite().trim());
        }
    }

    private UserProfileResponse mapToResponse(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setBio(profile.getBio());
        response.setLocation(profile.getLocation());
        response.setFollowersCount(0);
        response.setFollowingCount(0);
        response.setPostsCount(user.getPosts() != null ? user.getPosts().size() : 0);
        return response;
    }
}
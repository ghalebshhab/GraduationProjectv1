package com.start.demo.Services.Users.Profile;

import com.start.demo.DTOs.UserProfile.UpdateUserProfileRequest;
import com.start.demo.DTOs.UserProfile.UserProfileResponse;
import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserProfile;
import com.start.demo.Entities.Users.UserProfileRepository;
import com.start.demo.Entities.Users.UserRepository;
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
    public UserProfileResponse getMyProfile(String usernameFromToken) {
        User user = userRepository.findByUsername(usernameFromToken).orElse(null);
        if (user == null) {
            return null;
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return null;
        }

        return mapToResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return null;
        }

        return mapToResponse(user, profile);
    }

    @Transactional
    @Override
    public UserProfileResponse updateMyProfile(String usernameFromToken, UpdateUserProfileRequest request) {
        User user = userRepository.findByUsername(usernameFromToken).orElse(null);
        if (user == null) {
            return null;
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return null;
        }

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

        userRepository.save(user);
        userProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return null;
        }

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

        userRepository.save(user);
        userProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    private UserProfileResponse mapToResponse(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();

        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setProfileImageUrl(user.getProfileImageUrl());

        response.setBio(profile.getBio());
        response.setCoverImageUrl(profile.getCoverImageUrl());
        response.setLocation(profile.getLocation());
        response.setBirthDate(profile.getBirthDate());
        response.setWebsite(profile.getWebsite());

        return response;
    }
}
package com.jomap.backend.Services.Users.Profile;

import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import org.springframework.transaction.annotation.Transactional;

public interface UserProfileService {

    @Transactional(readOnly = true)
    UserProfileResponse getMyProfile(String usernameFromToken);

    UserProfileResponse getProfileByUserId(Long userId);

    @Transactional
    UserProfileResponse updateMyProfile(String usernameFromToken, UpdateUserProfileRequest request);

    UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request);
}
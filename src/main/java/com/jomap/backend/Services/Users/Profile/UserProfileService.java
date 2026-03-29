package com.jomap.backend.Services.Users.Profile;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import org.springframework.transaction.annotation.Transactional;

public interface UserProfileService {

    @Transactional(readOnly = true)
   ApiResponse<UserProfileResponse> getMyProfile(String usernameFromToken);

    ApiResponse<UserProfileResponse> getProfileByUserId(Long userId);

    @Transactional
    ApiResponse<UserProfileResponse> updateMyProfile(String usernameFromToken, UpdateUserProfileRequest request);

   ApiResponse< UserProfileResponse> updateProfile(Long userId, UpdateUserProfileRequest request);
}
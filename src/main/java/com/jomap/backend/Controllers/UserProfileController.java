package com.jomap.backend.Controllers;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Services.Users.Profile.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userProfileService.getProfileByUserId(userId);

        if (profile == null) {
            return ResponseEntity.status(404).body(
                    new ApiResponse<>(false, "User profile not found", null)
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User profile fetched successfully", profile)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse updatedProfile = userProfileService.updateProfile(userId, request);

        if (updatedProfile == null) {
            return ResponseEntity.status(404).body(
                    new ApiResponse<>(false, "User profile not found", null)
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User profile updated successfully", updatedProfile)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        String usernameFromToken = authentication.getName();

        UserProfileResponse response = userProfileService.updateMyProfile(usernameFromToken, request);

        if (response == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, "Profile not found", null));
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", response)
        );
    }
}
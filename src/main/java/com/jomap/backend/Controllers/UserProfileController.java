package com.jomap.backend.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Services.Users.Profile.UserProfileService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/profile")
@AllArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.getMyProfile(emailFromToken));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    /*@PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateMyProfile(emailFromToken, request));
    }*/

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateMyProfile(emailFromToken, request));
    }
    
}
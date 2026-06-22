package com.jomap.backend.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Services.Users.Profile.UserProfileService;
import com.jomap.backend.Services.Auth.TokenBlacklistService;
import com.jomap.backend.Services.Auth.JwtService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/profile")
@AllArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.getMyProfile(emailFromToken));
    }

    @GetMapping("/me/photo")
    public ResponseEntity<ApiResponse<String>> getMyProfilePhoto(Authentication authentication) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.getMyProfilePhoto(emailFromToken));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateMyProfile(emailFromToken, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyProfile(
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        String emailFromToken = authentication.getName();

        // Extract token and blacklist it upon deletion
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                java.util.Date expiration = jwtService.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expiration);
            } catch (Exception ignored) {
                // Token is invalid or already expired
            }
        }

        return ResponseEntity.ok(userProfileService.deleteMyProfile(emailFromToken));
    }
}
// Graduated Officially

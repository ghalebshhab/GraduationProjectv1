package com.jomap.backend.Services.Users.Profile;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Entities.Locations.LocationRepo;
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
    private final LocationRepo locationRepository; 

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
    public ApiResponse<String> getMyProfilePhoto(String usernameFromToken) {
        User user = userRepository.findByEmail(usernameFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ApiResponse.error("Profile not found");
        }

        return ApiResponse.success("Profile photo fetched successfully", profile.getProfileImageUrl());
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

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername().trim());
        }

        // 🎯 تعديل 1: حمينا رقم الهاتف من الفراغات ليتجاوز الـ Validation بنجاح ✅
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl().trim());
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            profile.setFirstName(request.getFirstName().trim());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName().trim());
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            profile.setGender(request.getGender());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation().trim());
        }

        if (request.getDateOfBirth() != null) {
            profile.setBirthDate(request.getDateOfBirth());
        }

        if (request.getInstagramUrl() != null) {
            profile.setInstagramUrl(request.getInstagramUrl().trim());
        }

        if (request.getFacebookUrl() != null) {
            profile.setFacebookUrl(request.getFacebookUrl().trim());
        }

        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl().trim());
        }
    }

    private UserProfileResponse mapToResponse(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber()); 
        response.setEmail(user.getEmail() != null ? user.getEmail() : "");
        response.setProfileImageUrl(profile.getProfileImageUrl());
        response.setBio(profile.getBio());
        response.setLocation(profile.getLocation());
        response.setFollowersCount(0);
        response.setFollowingCount(0);
        int activeUserPostsCount = 0;
        if (user.getPosts() != null) {
            activeUserPostsCount = (int) user.getPosts().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                    .filter(p -> {
                        String type = p.getType() != null ? p.getType().name().toUpperCase() : "";
                        String category = p.getCategory() != null ? p.getCategory().toUpperCase() : "";

                        // Exclude Activity and Offer posts entirely
                        if ("ACTIVITY".equals(category) || "OFFER".equals(category) || 
                            "ACTIVITY".equals(type) || "OFFER".equals(type)) {
                            return false;
                        }

                        return "USER".equals(type) || "COMMUNITY".equals(type) ||
                               "USER".equals(category) || "COMMUNITY".equals(category) || category.isEmpty();
                    })
                    .count();
        }
        response.setPostsCount(activeUserPostsCount);
        
        response.setRole(user.getRole() != null ? user.getRole().name() : "USER");

        locationRepository.findByOwnerId(user.getId()).ifPresent(loc -> {
            response.setLocationId(loc.getId());
        });

        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setGender(profile.getGender());
        response.setBirthDate(profile.getBirthDate() != null ? profile.getBirthDate().toString() : ""); 
        response.setInstagramUrl(profile.getInstagramUrl());
        response.setFacebookUrl(profile.getFacebookUrl());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        

        return response;
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteMyProfile(String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }
        user.setIsActive(false);
        userRepository.save(user);
        return ApiResponse.success("تم حذف الحساب بنجاح", "Account deleted successfully");
    }
}
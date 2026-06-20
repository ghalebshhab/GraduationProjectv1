package com.jomap.backend.Services.Users.Profile;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.UserProfile.UpdateUserProfileRequest;
import com.jomap.backend.DTOs.UserProfile.UserProfileResponse;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.Profile.UserProfile;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Users.UserBlockRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LocationRepo locationRepository;
    private final UserBlockRepository userBlockRepository;
    private final EntityManager entityManager;

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

        // isBlocked: check if the currently authenticated user has blocked this profile's user
        response.setIsBlocked(false);
        response.setIsBlockedByThem(false);
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String currentEmail = auth.getName();
                userRepository.findByEmail(currentEmail).ifPresent(currentUser -> {
                    if (!currentUser.getId().equals(user.getId())) {
                        // أنا حظرت هذا الشخص؟
                        boolean blocked = userBlockRepository.existsByBlockerAndBlocked(currentUser, user);
                        response.setIsBlocked(blocked);
                        // هذا الشخص حظرني؟
                        boolean blockedByThem = userBlockRepository.existsByBlockerAndBlocked(user, currentUser);
                        response.setIsBlockedByThem(blockedByThem);
                    }
                });
            }
        } catch (Exception ignored) { }

        return response;
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteMyProfile(String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // Deactivate the user account
        user.setIsActive(false);

        // Secure soft delete: Anonymize sensitive fields
        user.setUsername("deleted_user_" + user.getId());
        user.setEmail("deleted_" + user.getId() + "@jomap.com");
        user.setPhoneNumber("deleted_" + user.getId());
        user.setProfileImageUrl(null);

        if (user.getProfile() != null) {
            user.getProfile().setFirstName("Deleted");
            user.getProfile().setLastName("User");
            user.getProfile().setBio(null);
            user.getProfile().setLocation(null);
            user.getProfile().setProfileImageUrl(null);
            user.getProfile().setInstagramUrl(null);
            user.getProfile().setFacebookUrl(null);
            user.getProfile().setLinkedinUrl(null);
            user.getProfile().setBirthDate(null);
        }

        // Soft delete user's location (LocationList) and its related data (Offers, Activities)
        LocationList location = locationRepository.findByOwnerId(user.getId()).orElse(null);
        if (location != null) {
            entityManager.createQuery("UPDATE LocationList l SET l.isDeleted = true, l.deletedAt = :deletedAt WHERE l.id = :locationId")
                    .setParameter("locationId", location.getId())
                    .setParameter("deletedAt", java.time.LocalDateTime.now())
                    .executeUpdate();

            entityManager.createQuery("UPDATE Offer o SET o.isDeleted = true WHERE o.location = :location")
                    .setParameter("location", location)
                    .executeUpdate();

            entityManager.createQuery("UPDATE Activity a SET a.isDeleted = true WHERE a.locationId = :locationId")
                    .setParameter("locationId", location.getId())
                    .executeUpdate();
        }

        entityManager.createQuery("UPDATE Post p SET p.isDeleted = true WHERE p.author = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE PostComment pc SET pc.isDeleted = true WHERE pc.author = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE Story s SET s.isDeleted = true WHERE s.author = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE StoryReply sr SET sr.isDeleted = true WHERE sr.user = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE Activity a SET a.isDeleted = true WHERE a.createdBy = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE Offer o SET o.isDeleted = true WHERE o.createdBy = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("UPDATE Feedback f SET f.isDeleted = true WHERE f.user = :user")
                .setParameter("user", user)
                .executeUpdate();

        // Clear user favorites relationships (many-to-many join tables)
        if (user.getFavoriteLocations() != null) {
            user.getFavoriteLocations().clear();
        }
        if (user.getFavoritePlaces() != null) {
            user.getFavoritePlaces().clear();
        }
        if (user.getFavoriteEvents() != null) {
            user.getFavoriteEvents().clear();
        }
        if (user.getFavoriteOffers() != null) {
            user.getFavoriteOffers().clear();
        }

        // Hard delete interactions, friendships, blocks, notifications to maintain integrity and compliance
        entityManager.createQuery("DELETE FROM PostLikes pl WHERE pl.user = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("DELETE FROM StoryLike sl WHERE sl.user = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("DELETE FROM StoryView sv WHERE sv.user = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("DELETE FROM Notification n WHERE n.toUser = :user OR n.fromUser = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("DELETE FROM Friendship f WHERE f.requester = :user OR f.receiver = :user")
                .setParameter("user", user)
                .executeUpdate();

        entityManager.createQuery("DELETE FROM UserBlock ub WHERE ub.blocker = :user OR ub.blocked = :user")
                .setParameter("user", user)
                .executeUpdate();

        userRepository.save(user);
        return ApiResponse.success("تم حذف الحساب نهائياً بنجاح", null);
    }
}
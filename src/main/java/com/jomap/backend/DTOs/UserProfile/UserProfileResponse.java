package com.jomap.backend.DTOs.UserProfile;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileResponse {

    private Long userId;
    private String username;
    private String profileImageUrl;
    private String email;
    private String bio;
    private String location;
    private int followersCount;
    private int followingCount;
    private int postsCount;
}
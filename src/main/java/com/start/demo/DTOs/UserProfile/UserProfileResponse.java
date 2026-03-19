package com.start.demo.DTOs.UserProfile;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileResponse {

    private Long userId;

    private String username;
    private String email;
    private String phoneNumber;


    private String profileImageUrl;

    private String bio;
    private String coverImageUrl;
    private String location;
    private LocalDate birthDate;
    private String website;
}
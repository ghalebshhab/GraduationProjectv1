package com.jomap.backend.DTOs.UserProfile;

import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {

    private String profileImageUrl;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be at most 100 characters")
    private String username; 

    @Size(max = 20, message = "Phone number must be at most 20 digits")
    @Pattern(regexp = "^07[789][0-9]{7}$", message = "Invalid Jordanian phone number")
    private String phoneNumber;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "Gender must be either MALE or FEMALE")
    private String gender; 

    private LocalDate dateOfBirth; 

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @Size(max = 150, message = "Location must be at most 150 characters")
    private String location; 

    @Size(max = 500, message = "Instagram URL too long")
    private String instagramUrl;

    @Size(max = 500, message = "Facebook URL too long")
    private String facebookUrl;

    @Size(max = 500, message = "LinkedIn URL too long")
    private String linkedinUrl;
}
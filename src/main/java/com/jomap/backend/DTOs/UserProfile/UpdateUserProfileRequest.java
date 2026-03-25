package com.jomap.backend.DTOs.UserProfile;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {


    @Size(max = 100, message = "User name must be at most 100 characters")
    private String userName;

    @Size(max = 20, message = "Phone number must be at most 20 digits")
    @Pattern(regexp = "^07[789][0-9]{7}$", message = "Invalid Jordanian phone number")
    private String phoneNumber;

    private String profileImageUrl;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    private String coverImageUrl;

    @Size(max = 50, message = "Location must be at most 150 characters")
    private String location;

    private LocalDate birthDate;

    @Size(max = 255, message = "Website URL too long")
    private String website;
}
package com.jomap.backend.Entities.Users.Profile;

import java.time.LocalDate;

import com.jomap.backend.Entities.Users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 10) // يستقبل "MALE" أو "FEMALE"
    private String gender;

    @Column(name = "profile_image_url", columnDefinition = "TEXT") 
    private String profileImageUrl;

    @Column(length = 500)
    private String bio;

    @Column(length = 150)
    private String location;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;



    public UserProfile(User user, String firstName, String lastName, String gender, LocalDate birthDate, String profileImageUrl) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
    }

}
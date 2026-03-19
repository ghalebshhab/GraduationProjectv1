package com.start.demo.Entities.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Getter
    @Setter
    @Column(length = 500)
    private String bio;

    @Getter
    @Setter
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Getter
    @Setter
    @Column(length = 150)
    private String location;

    @Getter
    @Setter
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Getter
    @Setter
    @Column(length = 255)
    private String website;

}
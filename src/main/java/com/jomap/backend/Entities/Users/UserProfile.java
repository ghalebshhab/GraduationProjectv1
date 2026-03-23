package com.jomap.backend.Entities.Users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "user_profiles")
public class UserProfile {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @Column
    private Long userId;


    @Column(length = 500)
    private String bio;


    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;


    @Column(length = 150)
    private String location;


    @Column(name = "birth_date")
    private LocalDate birthDate;


    @Column(length = 255)
    private String website;

}
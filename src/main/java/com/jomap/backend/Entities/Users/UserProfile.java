package com.jomap.backend.Entities.Users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.sql.results.graph.Fetch;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 500)
    private String bio;

    @Column(name = "cover_image_url", columnDefinition = "LONGTEXT")
    private String coverImageUrl;

    @Column(length = 150)
    private String location;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 255)
    private String website;

}
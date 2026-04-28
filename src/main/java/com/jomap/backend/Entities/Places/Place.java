package com.jomap.backend.Entities.Places;

import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 3000)
    private String description;

    private String email;

    private String phoneNumber;

    @Column(length = 1000)
    private String imageUrl;

    private Double latitude;

    private Double longitude;

    private String governorate;

    @Enumerated(EnumType.STRING)
    private PlaceCategory category;

    private Double rating = 0.0;

    private Integer reviewCount = 0;

    private Boolean active = true;

    private Boolean approved = false;

    @Column(length = 3000)
    private String ownerUpdate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true, nullable = false)
    private User owner;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (rating == null) rating = 0.0;
        if (reviewCount == null) reviewCount = 0;
        if (active == null) active = true;
        if (approved == null) approved = false;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

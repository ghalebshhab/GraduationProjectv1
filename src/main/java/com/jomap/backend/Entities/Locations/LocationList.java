package com.jomap.backend.Entities.Locations;

import java.time.LocalDateTime;

import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "location_lists")
@Getter
@Setter
@NoArgsConstructor
public class LocationList {

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
    private String logoUrl; 

    @Column(name = "cover_url",length = 1000)
    private String coverUrl;

    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @Enumerated(EnumType.STRING)
    private LocationCategory category;

    @Enumerated(EnumType.STRING)
    private LocationStatus status; 

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private LocationStatus previousStatus; 

    private String facebookUrl;
    private String instagramUrl;
    private String linkedInUrl;
    private String workingHours;

    private Double rating = 0.0;
    private Integer reviewCount = 0;
    
    @Column(name = "profile_visits")
    private Integer profileVisits = 0;

    private Boolean active = true;
    private Boolean approved = false;

    @Column(length = 3000)
    private String ownerUpdate;

    @Column(length = 2000)
    private String rejectionReason;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true, nullable = false)
    private User owner;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        this.status = LocationStatus.PENDING; 
        this.approved = false; 
        this.active = true;

        if (rating == null) rating = 0.0;
        if (reviewCount == null) reviewCount = 0;
        if (profileVisits == null) profileVisits = 0;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
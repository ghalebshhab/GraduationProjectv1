package com.jomap.backend.Entities.Locations;

import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_lists")
@Data
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

    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @Enumerated(EnumType.STRING)
    private LocationCategory category;

    // الحقل الجديد لضبط دورة حياة الموقع
    @Enumerated(EnumType.STRING)
    private LocationStatus status; 

    private String facebookUrl;
    private String instagramUrl;
    private String linkedInUrl;
    private String workingHours;

    private Double rating = 0.0;
    private Integer reviewCount = 0;
    
    // بضلوا موجودين للتحكم البرمجي السريع، بس الـ status هو الحكم
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
        
        // عند الإنشاء: الحالة دائماً بانتظار الموافقة
        this.status = LocationStatus.PENDING; 
        this.approved = false; 
        this.active = true; // موجود في النظام كـ "سجل" ولكن غير منشور

        if (rating == null) rating = 0.0;
        if (reviewCount == null) reviewCount = 0;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
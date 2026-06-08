package com.jomap.backend.DTOs.Locations;

import lombok.Data;
import java.time.LocalDateTime;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.Entities.Locations.LocationStatus;

@Data
public class LocationResponse {
    private String logoUrl; 
    private String coverUrl;
    private Long locationId;
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Long governorateId;   
    private String governorateName;
    private LocationCategory category;
    private LocationStatus status;
    private Boolean isActive;
    private Boolean isApproved;
    
    private String facebookUrl;
    private String instagramUrl;
    private String linkedInUrl;
    private String workingHours;

    private Double rating;
    private Integer reviewCount;
    private Integer profileVisits;
    private String ownerUpdate;
    private Long ownerId;
    private String ownerName;
    private String ownerProfileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String newToken;
}
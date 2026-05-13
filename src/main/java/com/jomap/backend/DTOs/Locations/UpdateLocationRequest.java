package com.jomap.backend.DTOs.Locations;

import com.jomap.backend.Entities.Locations.LocationCategory;
import lombok.Data;

@Data
public class UpdateLocationRequest {
    private String logoUrl; 
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    
    private Long governorateId; 
    private LocationCategory category;

    private String facebookUrl;
    private String instagramUrl;
    private String linkedInUrl;
    private String workingHours;

    private String ownerUpdate;
}
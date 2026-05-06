package com.jomap.backend.DTOs.Places;

import com.jomap.backend.Entities.Places.PlaceCategory;
import lombok.Data;

@Data
public class UpdatePlaceRequest {
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String governorate;
    private PlaceCategory category;
    private String ownerUpdate;
}

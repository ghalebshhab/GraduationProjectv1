package com.jomap.backend.DTOs.Places;

import com.jomap.backend.Entities.Places.PlaceCategory;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PlaceResponse {

    private Long id;
    private String name;
    private String description;
    private String email;
    private String phoneNumber;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String governorate;
    private PlaceCategory category;
    private Double rating;
    private Integer reviewCount;
    private Boolean active;
    private Boolean approved;
    private String ownerUpdate;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.jomap.backend.DTOs.Governorate;

import com.jomap.backend.Entities.Locations.LocationCategory;

import lombok.Data;

@Data
public class PlaceRequest {
    private String name;
    private String description;
    private String imageUrl;
    private LocationCategory category;
}
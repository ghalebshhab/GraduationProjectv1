package com.jomap.backend.DTOs.Gove;

import com.jomap.backend.Entities.Locations.LocationCategory;

import lombok.Data;

@Data
public class PlaceRequestDto {
    private String name;
    private String description;
    private String imageUrl;
    private LocationCategory category;
}
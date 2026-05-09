package com.jomap.backend.DTOs.Gove;

import com.jomap.backend.Entities.Places.PlaceCategory;
import lombok.Data;

@Data
public class PlaceRequestDto {
    private String name;
    private String description;
    private String imageUrl;
    private PlaceCategory category;
}

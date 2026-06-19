package com.jomap.backend.DTOs.Governorate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String category; 
    private boolean isUserGenerated;
    private String logoUrl;
    private Double rating;
    private String governorateName;
}
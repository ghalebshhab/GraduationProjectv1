package com.jomap.backend.DTOs.Ai;

import lombok.Data;

@Data
public class ImprovePlaceDescriptionRequest {

    private String placeName;
    private String currentDescription;
    private String category;
    private String governorate;
}
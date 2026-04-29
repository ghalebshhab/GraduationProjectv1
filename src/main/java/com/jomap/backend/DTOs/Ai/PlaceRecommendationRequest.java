package com.jomap.backend.DTOs.Ai;

import lombok.Data;

@Data
public class PlaceRecommendationRequest {

    private String userMessage;
    private String governorate;
    private String category;
}
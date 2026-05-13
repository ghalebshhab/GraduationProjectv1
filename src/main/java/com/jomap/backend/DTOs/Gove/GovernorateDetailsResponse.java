package com.jomap.backend.DTOs.Gove;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Locations.LocationResponse;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GovernorateDetailsResponse {
    private Long id;
    private String name;
    private List<String> images;
    private List<LocationResponse> suggestions;
    private List<LocationResponse> historicalPlaces;

    private Map<String, List<LocationResponse>> placesByCategory;

    private List<ActivityResponse> activities;
}
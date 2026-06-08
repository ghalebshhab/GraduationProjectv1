package com.jomap.backend.DTOs.Governorate;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GovernorateDetailsResponse {
    private Long id;
    private String name;
    private String description; 
    private List<String> images;
    
    private List<PlaceResponse> suggestedPlaces;
    private List<PlaceResponse> historicalPlaces;
    private List<PlaceResponse> suggestedTeams;

    private List<ActivityResponse> upcomingActivities; 
    private List<com.jomap.backend.DTOs.Offers.OfferResponse> offers;
}
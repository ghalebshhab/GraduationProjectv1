package com.jomap.backend.DTOs.Gove;

import com.jomap.backend.DTOs.Events.EventResponse;
import com.jomap.backend.DTOs.Places.PlaceResponse;
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


    private Map<String, List<PlaceResponse>> placesByCategory;

    private List<EventResponse> events;
}

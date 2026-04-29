package com.jomap.backend.Services.Ai;

import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.Ai.ImprovePlaceDescriptionRequest;
import com.jomap.backend.DTOs.Ai.PlaceRecommendationRequest;
import com.jomap.backend.DTOs.ApiResponse;

public interface AiService {

    ApiResponse<AiChatResponse> chat(AiChatRequest request);

    ApiResponse<AiChatResponse> recommendPlaces(PlaceRecommendationRequest request);

    ApiResponse<AiChatResponse> explainPlace(Long placeId);

    ApiResponse<AiChatResponse> improvePlaceDescription(ImprovePlaceDescriptionRequest request);
}
package com.jomap.backend.Services.Places;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Places.CreatePlaceRequest;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.DTOs.Places.UpdatePlaceRequest;
import com.jomap.backend.Entities.Places.PlaceCategory;

import java.util.List;

public interface PlaceService {
    ApiResponse<PlaceResponse> createPlace(CreatePlaceRequest request, String currentUserEmail);

    ApiResponse<PlaceResponse> updatePlace(Long placeId, UpdatePlaceRequest request, String currentUserEmail);

    ApiResponse<PlaceResponse> getPlaceById(Long placeId);

    ApiResponse<List<PlaceResponse>> getPlaces(String governorate, PlaceCategory category);

    ApiResponse<PlaceResponse> getMyPlace(String currentUserEmail);

    ApiResponse<PlaceResponse> approvePlace(Long placeId);

    ApiResponse<PlaceResponse> deactivatePlace(Long placeId, String currentUserEmail);
}

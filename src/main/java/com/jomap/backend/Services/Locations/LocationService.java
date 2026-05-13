package com.jomap.backend.Services.Locations;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Locations.LocationCategory;

import java.util.List;

public interface LocationService {
    ApiResponse<LocationResponse> createLocation(CreateLocationRequest request, String currentUserEmail);

    ApiResponse<LocationResponse> updateLocation(Long locationId, UpdateLocationRequest request, String currentUserEmail);

    ApiResponse<LocationResponse> getLocationById(Long locationId);

    ApiResponse<List<LocationResponse>> getLocations(Long governorateId, LocationCategory category);

    ApiResponse<LocationResponse> getMyLocation(String currentUserEmail);

    ApiResponse<LocationResponse> approveLocation(Long locationId);

    ApiResponse<LocationResponse> deactivateLocation(Long locationId, String currentUserEmail);
}
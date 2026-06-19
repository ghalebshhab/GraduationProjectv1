package com.jomap.backend.Services.Locations;

import java.util.List;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Locations.LocationCategory;

public interface LocationService {
    ApiResponse<LocationResponse> createLocation(CreateLocationRequest request, String currentUserEmail);

    ApiResponse<LocationResponse> updateLocation(Long locationId, UpdateLocationRequest request, String currentUserEmail);

    ApiResponse<LocationResponse> getLocationById(Long locationId);

    ApiResponse<List<LocationResponse>> getLocations(Long governorateId, LocationCategory category);

    ApiResponse<String> toggleFavoriteLocation(Long locationId, String userEmail);

    ApiResponse<List<LocationResponse>> getFavoriteLocations(String userEmail);

    ApiResponse<LocationResponse> getMyLocation(String currentUserEmail);

    ApiResponse<LocationResponse> approveLocation(Long locationId);

    // 🟢 الدالة الموحدة المعتمدة رسمياً لكل الحالات (تنشيط / تعطيل / حذف)
    ApiResponse<LocationResponse> changeLocationStatus(Long id, String status, String currentUserEmail);

    ApiResponse<LocationResponse> updateCover(Long locationId, UpdateLocationRequest request, String currentUserEmail);

    ApiResponse<LocationResponse> updateLogo(Long locationId, UpdateLocationRequest request, String currentUserEmail);

    ApiResponse<java.util.List<com.jomap.backend.DTOs.Locations.LocationFollowerResponse>> getLocationFollowers(Long locationId, String currentUserEmail);

    ApiResponse<String> followLocation(Long locationId, String userEmail);
    ApiResponse<String> unfollowLocation(Long locationId, String userEmail);
    ApiResponse<String> blockLocation(Long locationId, String userEmail);
    ApiResponse<String> unblockLocation(Long locationId, String userEmail);
}
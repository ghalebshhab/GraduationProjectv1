package com.jomap.backend.Services.Fav;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Fav.FavoritesDataDto;

public interface FavoritesService {

    ApiResponse<FavoritesDataDto> getMyFavorites(String email);

    ApiResponse<Void> addFavoriteLocation(Long locationId, String email);

    ApiResponse<Void> removeFavoriteLocation(Long locationId, String email);

    ApiResponse<Void> addFavoritePlace(Long placeId, String email);

    ApiResponse<Void> removeFavoritePlace(Long placeId, String email);

    ApiResponse<Void> addFavoriteActivity(Long activityId, String email);

    ApiResponse<Void> removeFavoriteActivity(Long activityId, String email);
}

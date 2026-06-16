package com.jomap.backend.DTOs.Fav;

import lombok.Data;

import java.util.List;

@Data
public class FavoritesDataDto{
private List<FavoriteLocationDto> locations;
private List<FavoriteEventDto> events;
private List<FavoritePostDto> posts;
}

package com.jomap.backend.DTOs.Fav;

import lombok.Data;

import java.util.List;

@Data
public class FavoritesDataDto{
private List<FavoritePlaceDto> places;
private List<FavoriteEventDto> events;
private List<FavoritePostDto> posts;
}

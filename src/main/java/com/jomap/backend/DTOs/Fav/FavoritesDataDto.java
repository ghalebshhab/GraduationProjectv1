package com.jomap.backend.DTOs.Fav;

import lombok.Data;

import java.util.List;

@Data
public class FavoritesDataDto{
private List<FavoriteLocationDto> locations;
private List<FavoriteActivityDto> activities;
private List<FavoriteOfferDto> offers;
private List<FavoritePostDto> posts;
}

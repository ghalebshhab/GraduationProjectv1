package com.jomap.backend.DTOs.Fav;

import lombok.Data;

@Data
public class FavoritePlaceDto {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private Double rating;
    private Double latitude;
    private Double longitude;
    private String placeType;
}

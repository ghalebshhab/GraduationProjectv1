package com.jomap.backend.DTOs.search;

import lombok.Data;

@Data
public class SearchItem {

    private Long id;
    private String title;
    private String subTitle;
    private int imageRes;
    private String imageUrl;
    private SearchType type;
    private Double rating;
    private Double distanceKm;
    private Double distance;
    private String eventDate;
    private String locationName;
    private String friendshipStatus;
    private String category;
    @com.fasterxml.jackson.annotation.JsonProperty("coverUrl")
    private String coverUrl;

    @com.fasterxml.jackson.annotation.JsonProperty("isOpenNow")
    private Boolean isOpenNow;

    @com.fasterxml.jackson.annotation.JsonProperty("reviewCount")
    private Integer reviewCount;

    @com.fasterxml.jackson.annotation.JsonProperty("isFavorite")
    private Boolean isFavorite;

    @com.fasterxml.jackson.annotation.JsonProperty("governorateName")
    private String governorateName;
}

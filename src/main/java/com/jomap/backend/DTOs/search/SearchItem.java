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
    @com.fasterxml.jackson.annotation.JsonProperty("activityDate")
    private String activityDate;
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

    @com.fasterxml.jackson.annotation.JsonProperty("startDate")
    private String startDate;

    @com.fasterxml.jackson.annotation.JsonProperty("endDate")
    private String endDate;

    @com.fasterxml.jackson.annotation.JsonProperty("price")
    private Double price;

    @com.fasterxml.jackson.annotation.JsonProperty("activityLocation")
    private String activityLocation;

    @com.fasterxml.jackson.annotation.JsonProperty("itemsCount")
    private Integer itemsCount;

    @com.fasterxml.jackson.annotation.JsonProperty("status")
    private String status;
}

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
}



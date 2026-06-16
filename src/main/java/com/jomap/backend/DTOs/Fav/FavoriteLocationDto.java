package com.jomap.backend.DTOs.Fav;

import lombok.Data;

@Data
public class FavoriteLocationDto {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private Double rating;
    private Double latitude;
    private Double longitude;
    private String placeType;

    // الحقول الإضافية البريميوم المطلوبة من قبل فريق الأندرويد
    private String coverUrl;
    private String governorateName;
    private Boolean isOpenNow;
    private Integer reviewCount;
    private String locationType;
}

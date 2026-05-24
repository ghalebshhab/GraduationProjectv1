package com.jomap.backend.DTOs.Fav;

import lombok.Data;

@Data
public class FavoriteEventDto {
    private Long id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String locationName;
    private String imageUrl;
    private String organizer;
    private int attendeesCount;
    private String price;
}

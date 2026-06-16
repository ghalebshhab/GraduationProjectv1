package com.jomap.backend.DTOs.Fav;

import lombok.Data;
import java.util.List;

@Data
public class FavoriteActivityDto {
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
    private List<com.jomap.backend.DTOs.Activities.ActivitySchedule> schedules;
}

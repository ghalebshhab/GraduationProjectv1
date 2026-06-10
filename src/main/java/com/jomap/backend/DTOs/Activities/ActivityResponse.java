package com.jomap.backend.DTOs.Activities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponse {

    private Long id;

    private String title;

    private String description;

    private String scheduleType;
    
    private Integer totalActualDays;

    private java.util.List<ActivitySchedule> schedules;

    private String activityLocation;

    private Long governorateId;
    private String governorateName;

    private String imageUrl;

    private Double latitude;

    private Double longitude;

    private Double price;

    private Integer maxCapacity;

    private Integer attendeesCount; 

    private Long statusId;

    private Long createdById;

    private String createdByUsername;

    private String locationPhone;

    private String locationEmail;
}
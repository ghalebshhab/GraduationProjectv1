package com.jomap.backend.DTOs.Events;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponse {

    private Long id;

    private String title;

    private String description;

    private String date;

    private String time;

    private String locationName;

    private String governorate;

    private String imageUrl;

    private Double latitude;

    private Double longitude;

    private String status;

    private Long createdById;

    private String createdByUsername;
}
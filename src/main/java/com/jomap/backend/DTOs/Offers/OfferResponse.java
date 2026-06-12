package com.jomap.backend.DTOs.Offers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
public class OfferResponse {

    private Long id;
    private String title;
    private String description;
    private String scheduleType;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private List<OfferProductResponse> products;
    private Long locationId;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Long governorateId;
    private String governorateName;
    private Long statusId; 
    private Long createdById;
    private String createdByUsername;
    private String phoneNumber;
    private String locationPhone;
    private Integer viewsCount;
    private Integer clicksCount;
    private String cancelledAt;
    private Boolean isRenewed;
}
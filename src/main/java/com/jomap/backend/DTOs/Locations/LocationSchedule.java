package com.jomap.backend.DTOs.Locations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSchedule {
    private String dayName;
    private String startTime;
    private String endTime;
    private Boolean isClosed;
}

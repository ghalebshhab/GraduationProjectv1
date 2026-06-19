package com.jomap.backend.DTOs.Locations;

import com.jomap.backend.Entities.Locations.LocationCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockedLocationResponse {
    private Long locationId;
    private String name;
    private String logoUrl;
    private String coverUrl;
    private LocationCategory category;
}

package com.jomap.backend.DTOs.Locations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationFollowerResponse {
    private Long id;
    private String username;
    private String profileImage;
    private String followedAt;
}

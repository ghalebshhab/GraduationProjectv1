package com.jomap.backend.DTOs.Locations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationFollowerResponse {
    private Long userId;
    private String username;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
}

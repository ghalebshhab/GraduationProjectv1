package com.jomap.backend.DTOs.Stories.Views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryViewerResponse {
    private Long userId;
    private String username;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String viewedAt;
}

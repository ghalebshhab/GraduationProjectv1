package com.jomap.backend.DTOs.Stories.Views;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
@Data
@AllArgsConstructor
public class StoryViewResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private Instant viewedAt;

   }
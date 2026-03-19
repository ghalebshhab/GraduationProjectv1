package com.start.demo.DTOs.Stories.Likes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
public class StoryLikeResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private Instant createdAt;

  }
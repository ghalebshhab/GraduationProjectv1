package com.start.demo.DTOs.Stories.Replies;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
@Data
@AllArgsConstructor
public class StoryReplyResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private String content;
    private Boolean isDeleted;
    private Instant createdAt;
}
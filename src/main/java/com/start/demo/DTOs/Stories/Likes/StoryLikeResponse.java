package com.start.demo.DTOs.Stories.Likes;

import java.time.Instant;

public class StoryLikeResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private Instant createdAt;

    public StoryLikeResponse() {
    }

    public StoryLikeResponse(Long id, Long storyId, Long userId, String username, Instant createdAt) {
        this.id = id;
        this.storyId = storyId;
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getStoryId() {
        return storyId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
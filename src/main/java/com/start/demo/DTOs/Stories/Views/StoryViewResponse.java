package com.start.demo.DTOs.Stories.Views;

import java.time.Instant;

public class StoryViewResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private Instant viewedAt;

    public StoryViewResponse() {
    }

    public StoryViewResponse(Long id, Long storyId, Long userId, String username, Instant viewedAt) {
        this.id = id;
        this.storyId = storyId;
        this.userId = userId;
        this.username = username;
        this.viewedAt = viewedAt;
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

    public Instant getViewedAt() {
        return viewedAt;
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

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }
}
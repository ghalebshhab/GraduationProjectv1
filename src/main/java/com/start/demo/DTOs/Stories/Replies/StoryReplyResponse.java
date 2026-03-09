package com.start.demo.DTOs.Stories.Replies;

import java.time.Instant;

public class StoryReplyResponse {

    private Long id;
    private Long storyId;
    private Long userId;
    private String username;
    private String content;
    private Boolean isDeleted;
    private Instant createdAt;

    public StoryReplyResponse() {
    }

    public StoryReplyResponse(Long id, Long storyId, Long userId, String username,
                              String content, Boolean isDeleted, Instant createdAt) {
        this.id = id;
        this.storyId = storyId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.isDeleted = isDeleted;
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

    public String getContent() {
        return content;
    }

    public Boolean getDeleted() {
        return isDeleted;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
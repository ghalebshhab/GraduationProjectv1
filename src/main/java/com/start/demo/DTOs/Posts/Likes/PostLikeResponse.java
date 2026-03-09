package com.start.demo.DTOs.Posts.Likes;


import java.time.Instant;

public class PostLikeResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private Instant createdAt;

    public PostLikeResponse() {
    }

    public PostLikeResponse(Long id, Long postId, Long userId, String username, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
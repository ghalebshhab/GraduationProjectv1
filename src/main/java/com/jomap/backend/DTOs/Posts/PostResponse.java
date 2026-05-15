package com.jomap.backend.DTOs.Posts;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;

    private Long authorId;
    private String authorUsername;
    private String authorEmail;
    private String authorProfileImageUrl;

    private String content;
    private String mediaUrl;
    private String type;

    private Instant createdAt;
    private Instant updatedAt;

    private Long likeCount;
    private Long commentCount;

    private boolean likedByCurrentUser;
    private boolean savedByCurrentUser;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double distanceKm;
    private String scoreReason;
    

}
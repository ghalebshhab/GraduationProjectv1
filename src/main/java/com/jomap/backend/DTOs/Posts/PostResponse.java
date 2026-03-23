package com.jomap.backend.DTOs.Posts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
}
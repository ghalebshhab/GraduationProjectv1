package com.jomap.backend.DTOs.Posts.Likes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeResponse {

    private Long    id;

    // Post that was liked
    private Long    postId;

    // User who liked it
    private Long    userId;
    private String  username;

    // When the like was created
    private Instant createdAt;
}
package com.jomap.backend.DTOs.Posts.Comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentResponse {

    private Long    id;

    // Post this comment belongs to
    private Long    postId;

    // Author info
    private Long    authorId;
    private String  authorUsername;
    private String  authorEmail;
    private String  authorProfileImageUrl;

    // Content
    private String  content;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
}
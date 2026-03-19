package com.start.demo.DTOs.Posts.Comments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
public class PostCommentResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String content;
    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;


}
package com.start.demo.DTOs.Posts.Likes;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
public class PostLikeResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private Instant createdAt;


}
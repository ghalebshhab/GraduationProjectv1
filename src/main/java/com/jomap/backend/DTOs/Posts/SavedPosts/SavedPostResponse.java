package com.jomap.backend.DTOs.Posts.SavedPosts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedPostResponse {

    private Long id;
    private Long userId;
    private Long postId;
    private String content;
    private String mediaUrl;
    private String type;
    private Long authorId;
    private String authorUsername;
    private Instant postCreatedAt;
    private Instant savedAt;


}
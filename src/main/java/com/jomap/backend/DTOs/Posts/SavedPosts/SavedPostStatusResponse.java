package com.jomap.backend.DTOs.Posts.SavedPosts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedPostStatusResponse {
    private Long userId;
    private Long postId;
    private boolean saved;

}

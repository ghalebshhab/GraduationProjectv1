package com.jomap.backend.DTOs.Posts.SavedPosts;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSavedPostRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "postId is required")
    private Long postId;


}
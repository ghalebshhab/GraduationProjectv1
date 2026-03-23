package com.jomap.backend.DTOs.Posts.Comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePostCommentRequest {



    @NotBlank
    @Size(max = 1000)
    private String content;



}

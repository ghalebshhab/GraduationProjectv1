package com.start.demo.DTOs.Posts.Comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdatePostCommentRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;


}
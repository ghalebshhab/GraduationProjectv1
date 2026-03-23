package com.jomap.backend.DTOs.Stories.Replies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateStoryReplyRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;


}
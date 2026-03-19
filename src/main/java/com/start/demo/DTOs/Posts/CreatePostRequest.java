package com.start.demo.DTOs.Posts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePostRequest {



    @NotBlank
    @Size(max = 2000)
    private String content;

    @Size(max = 500)
    private String mediaUrl; // optional

    private String type;     // COMMUNITY / EVENT / OFFER




}
package com.jomap.backend.DTOs.Posts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePostRequest {

    @NotBlank
    @Size(max = 2000)
    private String content;

    @Size(max = 500)
    private String mediaUrl;

    private String type; // COMMUNITY / EVENT / OFFER


}
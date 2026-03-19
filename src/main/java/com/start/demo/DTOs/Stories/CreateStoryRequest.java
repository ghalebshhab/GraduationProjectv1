package com.start.demo.DTOs.Stories;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateStoryRequest {


    @NotBlank
    @Size(max = 500)
    private String mediaUrl;

    @Size(max = 500)
    private String caption;

    // optional: hours (default 24) - simplest control
    private Integer expiresInHours;




}
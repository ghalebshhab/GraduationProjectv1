package com.jomap.backend.DTOs.Ai;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiChatRequest {

    @NotBlank
    private String message;
}
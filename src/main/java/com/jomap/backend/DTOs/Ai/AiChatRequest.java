package com.jomap.backend.DTOs.Ai;

import lombok.Data;

@Data
public class AiChatRequest {

    private String message;
    private String governorate;
    private String category;
}
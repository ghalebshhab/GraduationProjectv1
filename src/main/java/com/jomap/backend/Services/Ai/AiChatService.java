package com.jomap.backend.Services.Ai;

import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.ApiResponse;

public interface AiChatService {

    ApiResponse<AiChatResponse> chat(AiChatRequest request);
}
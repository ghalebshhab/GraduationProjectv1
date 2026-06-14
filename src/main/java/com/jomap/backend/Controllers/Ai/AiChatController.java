package com.jomap.backend.Controllers.Ai;

import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Ai.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return aiChatService.chat(request);
    }
}
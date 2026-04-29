package com.jomap.backend.Controllers.Ai;


import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.Ai.ImprovePlaceDescriptionRequest;
import com.jomap.backend.DTOs.Ai.PlaceRecommendationRequest;
import com.jomap.backend.Services.Ai.AiService;
import com.jomap.backend.DTOs.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @RequestBody AiChatRequest request
    ) {
        return ResponseEntity.ok(aiService.chat(request));
    }

    @PostMapping("/recommend-places")
    public ResponseEntity<ApiResponse<AiChatResponse>> recommendPlaces(
            @RequestBody PlaceRecommendationRequest request
    ) {
        return ResponseEntity.ok(aiService.recommendPlaces(request));
    }

    @GetMapping("/explain-place/{placeId}")
    public ResponseEntity<ApiResponse<AiChatResponse>> explainPlace(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(aiService.explainPlace(placeId));
    }

    @PostMapping("/owner/improve-description")
    public ResponseEntity<ApiResponse<AiChatResponse>> improvePlaceDescription(
            @RequestBody ImprovePlaceDescriptionRequest request
    ) {
        return ResponseEntity.ok(aiService.improvePlaceDescription(request));
    }
}
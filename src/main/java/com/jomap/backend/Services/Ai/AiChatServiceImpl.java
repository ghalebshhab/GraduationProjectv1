package com.jomap.backend.Services.Ai;

import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Override
    public ApiResponse<AiChatResponse> chat(AiChatRequest request) {

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.error("Message is required");
        }

        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(geminiApiUrl)
                    .defaultHeader("x-goog-api-key", geminiApiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            String systemInstruction =
                    "You are JO MAP AI assistant. " +
                            "Help users explore Jordan. " +
                            "Answer about tourist places, restaurants, hotels, markets, activities, and travel tips inside Jordan. " +
                            "Keep answers short, friendly, and useful.";

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of(
                                                    "text", systemInstruction + "\n\nUser question: " + request.getMessage().trim()
                                            )
                                    )
                            )
                    )
            );

            Map response = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String answer = extractAnswer(response);

            if (answer == null || answer.trim().isEmpty()) {
                return ApiResponse.error("AI did not return an answer");
            }

            AiChatResponse aiChatResponse = new AiChatResponse(answer);

            return ApiResponse.success("AI response generated successfully", aiChatResponse);

        } catch (Exception e) {
            return ApiResponse.error("Failed to generate AI response");
        }
    }

    private String extractAnswer(Map response) {

        if (response == null || !response.containsKey("candidates")) {
            return null;
        }

        List candidates = (List) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Map firstCandidate = (Map) candidates.get(0);

        if (!firstCandidate.containsKey("content")) {
            return null;
        }

        Map content = (Map) firstCandidate.get("content");

        if (!content.containsKey("parts")) {
            return null;
        }

        List parts = (List) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return null;
        }

        Map firstPart = (Map) parts.get(0);

        if (!firstPart.containsKey("text")) {
            return null;
        }

        return firstPart.get("text").toString();
    }
}
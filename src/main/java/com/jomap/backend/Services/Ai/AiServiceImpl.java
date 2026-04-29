package com.jomap.backend.Services.Ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jomap.backend.DTOs.Ai.AiChatRequest;
import com.jomap.backend.DTOs.Ai.AiChatResponse;
import com.jomap.backend.DTOs.Ai.ImprovePlaceDescriptionRequest;
import com.jomap.backend.DTOs.Ai.PlaceRecommendationRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Places.Place;
import com.jomap.backend.Entities.Places.PlaceCategory;
import com.jomap.backend.Entities.Places.PlaceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private final PlaceRepository placeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;
    public AiServiceImpl(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ApiResponse<AiChatResponse> chat(AiChatRequest request) {

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ApiResponse.error("Message is required");
        }

        String prompt = """
                You are JO MAP AI assistant.
                JO MAP is an application for exploring Jordan governorates, tourism places,
                restaurants, hotels, markets, gyms, events, posts, and location profiles.

                Your job:
                - Explain app features clearly.
                - Suggest places in Jordan.
                - Be friendly and practical.
                - If the user asks about a specific governorate, focus on it.
                - Keep the answer helpful and not too long.

                User message:
                %s
                """.formatted(request.getMessage());

        return generateAnswer(prompt);
    }

    @Override
    public ApiResponse<AiChatResponse> recommendPlaces(PlaceRecommendationRequest request) {

        if (request == null || request.getUserMessage() == null || request.getUserMessage().isBlank()) {
            return ApiResponse.error("User message is required");
        }

        List<Place> places = getPlacesForRecommendation(request);

        if (places.isEmpty()) {
            AiChatResponse response = new AiChatResponse();
            response.setAnswer("I could not find matching places in the database yet. Try another governorate or category.");
            return ApiResponse.success("AI recommendation generated successfully", response);
        }

        String placesText = places.stream()
                .map(place -> """
                        Place ID: %s
                        Name: %s
                        Governorate: %s
                        Category: %s
                        Rating: %s
                        Description: %s
                        """.formatted(
                        place.getId(),
                        place.getName(),
                        place.getGovernorate(),
                        place.getCategory(),
                        place.getRating(),
                        place.getDescription()
                ))
                .toList()
                .toString();

        String prompt = """
                You are JO MAP AI recommendation assistant.

                The user wants recommendations:
                %s

                Here are places from the JO MAP database:
                %s

                Task:
                - Recommend the best places from this list only.
                - Explain why each place is suitable.
                - Mention governorate and category.
                - Do not invent places outside the provided list.
                - Keep the answer organized.
                """.formatted(request.getUserMessage(), placesText);

        return generateAnswer(prompt);
    }

    @Override
    public ApiResponse<AiChatResponse> explainPlace(Long placeId) {

        if (placeId == null) {
            return ApiResponse.error("Place id is required");
        }

        var placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        Place place = placeOptional.get();

        String prompt = """
                You are JO MAP AI assistant.

                Explain this place to the user in a friendly way:

                Name: %s
                Governorate: %s
                Category: %s
                Rating: %s
                Description: %s
                Owner update: %s

                Include:
                - What this place is
                - Who it is suitable for
                - Why the user may visit it
                - Simple tips
                """.formatted(
                place.getName(),
                place.getGovernorate(),
                place.getCategory(),
                place.getRating(),
                place.getDescription(),
                place.getOwnerUpdate()
        );

        return generateAnswer(prompt);
    }

    @Override
    public ApiResponse<AiChatResponse> improvePlaceDescription(ImprovePlaceDescriptionRequest request) {

        if (request == null) {
            return ApiResponse.error("Request body is required");
        }

        if (request.getCurrentDescription() == null || request.getCurrentDescription().isBlank()) {
            return ApiResponse.error("Current description is required");
        }

        String prompt = """
                You are helping a JO MAP business owner improve a location profile.

                Place name: %s
                Category: %s
                Governorate: %s
                Current description:
                %s

                Rewrite the description to be:
                - Professional
                - Attractive
                - Clear
                - Suitable for a location profile
                - Around 4 to 6 lines
                """.formatted(
                request.getPlaceName(),
                request.getCategory(),
                request.getGovernorate(),
                request.getCurrentDescription()
        );

        return generateAnswer(prompt);
    }

    private List<Place> getPlacesForRecommendation(PlaceRecommendationRequest request) {

        boolean hasGovernorate = request.getGovernorate() != null && !request.getGovernorate().isBlank();
        boolean hasCategory = request.getCategory() != null && !request.getCategory().isBlank();

        if (hasGovernorate && hasCategory) {
            try {
                PlaceCategory category = PlaceCategory.valueOf(request.getCategory().toUpperCase());
                return placeRepository.findByGovernorateIgnoreCaseAndCategoryAndActiveTrueAndApprovedTrue(
                        request.getGovernorate(),
                        category
                );
            } catch (IllegalArgumentException ex) {
                return placeRepository.findByGovernorateIgnoreCaseAndActiveTrueAndApprovedTrue(
                        request.getGovernorate()
                );
            }
        }

        if (hasGovernorate) {
            return placeRepository.findByGovernorateIgnoreCaseAndActiveTrueAndApprovedTrue(
                    request.getGovernorate()
            );
        }

        return placeRepository.findByActiveTrueAndApprovedTrue();
    }

    private ApiResponse<AiChatResponse> generateAnswer(String prompt) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            String requestBody = """
                    {
                      "model": "gpt-4o-mini",
                      "input": %s
                    }
                    """.formatted(objectMapper.writeValueAsString(prompt));

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/responses",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());

            String answer = extractText(root);

            AiChatResponse aiResponse = new AiChatResponse();
            aiResponse.setAnswer(answer);

            return ApiResponse.success("AI response generated successfully", aiResponse);

        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (errorMessage != null && errorMessage.contains("insufficient_quota")) {
                return ApiResponse.error("AI service is unavailable because the API quota is exceeded. Please check billing or API credits.");
            }

            if (errorMessage != null && errorMessage.contains("429")) {
                return ApiResponse.error("AI service is temporarily busy. Please try again later.");
            }

            return ApiResponse.error("AI service is currently unavailable. Please try again later.");
        }
    }

    private String extractText(JsonNode root) {

        JsonNode output = root.get("output");

        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");

                if (content != null && content.isArray()) {
                    for (JsonNode c : content) {
                        JsonNode text = c.get("text");

                        if (text != null) {
                            return text.asText();
                        }
                    }
                }
            }
        }

        return "Sorry, I could not generate an answer.";
    }
}
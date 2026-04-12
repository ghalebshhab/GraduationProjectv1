package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.CreateStoryRequest;
import com.jomap.backend.DTOs.Stories.StoryResponse;
import com.jomap.backend.Services.Community.Stories.StoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
@AllArgsConstructor
public class StoryController {

    private final StoryService storiesService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoryResponse>> create(@Valid @RequestBody CreateStoryRequest request) {
        return ResponseEntity.ok(storiesService.create(request));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<StoryResponse>>> active(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(storiesService.getActiveStories(page, size));
    }

    @GetMapping("/active/user/{userId}")
    public ResponseEntity<ApiResponse<List<StoryResponse>>> activeByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(storiesService.getActiveStoriesByUser(userId, page, size));
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long storyId) {
        return ResponseEntity.ok(storiesService.deleteStory(storyId));
    }
}
package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Community.Stories.Interaction.StoryViewService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
@AllArgsConstructor
public class StoryViewsController {

    private final StoryViewService views;

    @GetMapping("/{storyId}/views/count")
    public ResponseEntity<ApiResponse<Long>> count(@PathVariable Long storyId) {
        return ResponseEntity.ok(views.countByStoryId(storyId));
    }

    @PostMapping("/{storyId}/views")
    public ResponseEntity<ApiResponse<String>> addView(@PathVariable Long storyId) {
        return ResponseEntity.ok(views.addView(storyId));
    }

    @GetMapping("/{storyId}/views/exist")
    public ResponseEntity<ApiResponse<Boolean>> exist(@PathVariable Long storyId) {
        return ResponseEntity.ok(views.existsByStoryId(storyId));
    }
}
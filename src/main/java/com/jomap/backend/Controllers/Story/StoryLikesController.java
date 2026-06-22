package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Community.Stories.Interaction.StoryLikesService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@RestController
@RequestMapping("/api/stories")
@AllArgsConstructor
public class StoryLikesController {

    private final StoryLikesService likes;

    @GetMapping("/{storyId}/likes/count")
    public ResponseEntity<ApiResponse<Long>> count(@PathVariable Long storyId) {
        return ResponseEntity.ok(likes.countByStoryId(storyId));
    }

    @PostMapping("/{storyId}/likes")
    public ResponseEntity<ApiResponse<String>> addLike(@PathVariable Long storyId) {
        return ResponseEntity.ok(likes.addLike(storyId));
    }

    @GetMapping("/{storyId}/likes/exist")
    public ResponseEntity<ApiResponse<Boolean>> exist(@PathVariable Long storyId) {
        return ResponseEntity.ok(likes.existsByStoryId(storyId));
    }

    @DeleteMapping("/{storyId}/likes")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long storyId) {
        return ResponseEntity.ok(likes.deleteByStoryId(storyId));
    }
}
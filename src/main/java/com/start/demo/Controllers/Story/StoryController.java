package com.start.demo.Controllers.Story;

import com.start.demo.DTOs.Stories.CreateStoryRequest;
import com.start.demo.DTOs.Stories.StoryResponse;
import com.start.demo.Services.Community.Stories.StoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storiesService;

    public StoryController(StoryService storiesService) {
        this.storiesService = storiesService;
    }

    // POST /api/stories
    @PostMapping
    public StoryResponse create(@Valid @RequestBody CreateStoryRequest request) {
        return storiesService.create(request);
    }

    // GET /api/stories/active?page=0&size=20
    @GetMapping("/active")
    public List<StoryResponse> active(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return storiesService.getActiveStories(page, size);
    }

    // GET /api/stories/active/user/{userId}?page=0&size=20
    @GetMapping("/active/user/{userId}")
    public List<StoryResponse> activeByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return storiesService.getActiveStoriesByUser(userId, page, size);
    }

    // DELETE /api/stories/{storyId}
    @DeleteMapping("/{storyId}")
    public String delete(@PathVariable Long storyId) {
        return storiesService.deleteStory(storyId);
    }
}
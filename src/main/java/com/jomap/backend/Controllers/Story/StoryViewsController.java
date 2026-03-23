package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.Stories.Views.CreateStoryViewRequest;
import com.jomap.backend.DTOs.Stories.Views.StoryViewResponse;
import com.jomap.backend.Entities.Stories.StoryView;
import com.jomap.backend.Services.Community.Stories.Interaction.StoryViewService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
public class StoryViewsController {

    private final StoryViewService views;

    public StoryViewsController(StoryViewService views) {
        this.views = views;
    }

    // GET /api/stories/{storyId}/views/count
    @GetMapping("/{storyId}/views/count")
    public Long count(@PathVariable Long storyId) {
        return views.countByStoryId(storyId);
    }

    // POST /api/stories/{storyId}/views
    @PostMapping("/{storyId}/views")
    public StoryViewResponse addView(@PathVariable Long storyId,
                                     @Valid @RequestBody CreateStoryViewRequest request) {

        StoryView view = views.addView(storyId);

        return new StoryViewResponse(
                view.getId(),
                view.getStory().getId(),
                view.getUser().getId(),
                view.getUser().getUsername(),
                view.getViewedAt()
        );
    }

    // GET /api/stories/{storyId}/views/exist?userId=2
    @GetMapping("/{storyId}/views/exist")
    public Boolean exist(@PathVariable Long storyId, @RequestParam Long userId) {
        return views.existsByStoryId(storyId);
    }
}
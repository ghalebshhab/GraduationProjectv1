package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.Stories.Likes.CreateStoryLikeRequest;
import com.jomap.backend.DTOs.Stories.Likes.StoryLikeResponse;
import com.jomap.backend.Entities.Stories.StoryLike;
import com.jomap.backend.Services.Community.Stories.Interaction.StoryLikesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
public class StoryLikesController {

    private final StoryLikesService likes;

    public StoryLikesController(StoryLikesService likes) {
        this.likes = likes;
    }

    // GET /api/stories/{storyId}/likes/count
    @GetMapping("/{storyId}/likes/count")
    public Long count(@PathVariable Long storyId) {
        return likes.countByStoryId(storyId);
    }

    // POST /api/stories/{storyId}/likes
    @PostMapping("/{storyId}/likes")
    public StoryLikeResponse addLike(@PathVariable Long storyId,
                                     @Valid @RequestBody CreateStoryLikeRequest request) {

        StoryLike like = likes.addLike(storyId);

        return new StoryLikeResponse(
                like.getId(),
                like.getStory().getId(),
                like.getUser().getId(),
                like.getUser().getUsername(),
                like.getCreatedAt()
        );
    }

    // GET /api/stories/{storyId}/likes/exist?userId=2
    @GetMapping("/{storyId}/likes/exist")
    public Boolean exist(@PathVariable Long storyId, @RequestParam Long userId) {
        return likes.existsByStoryId(storyId);
    }

    // DELETE /api/stories/{storyId}/likes
    @DeleteMapping("/{storyId}/likes")
    public String delete(@PathVariable Long storyId,
                         @Valid @RequestBody CreateStoryLikeRequest request) {
        return likes.deleteByStoryId(storyId);
    }
}
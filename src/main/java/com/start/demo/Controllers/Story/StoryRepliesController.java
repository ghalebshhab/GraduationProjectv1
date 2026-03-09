package com.start.demo.Controllers.Story;

import com.start.demo.DTOs.Stories.Replies.StoryReplyResponse;
import com.start.demo.DTOs.Stories.Replies.CreateStoryReplyRequest;
import com.start.demo.DTOs.Stories.Replies.UpdateStoryReplyRequest;
import com.start.demo.Entities.Stories.StoryReply;
import com.start.demo.Services.Community.Stories.Interaction.StoryRepliesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/stories")
public class StoryRepliesController {

    private final StoryRepliesService replies;

    public StoryRepliesController(StoryRepliesService replies) {
        this.replies = replies;
    }

    // GET /api/stories/{storyId}/replies
    @GetMapping("/{storyId}/replies")
    public List<StoryReplyResponse> list(@PathVariable Long storyId) {
        return replies.findByStoryId(storyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET /api/stories/{storyId}/replies/count
    @GetMapping("/{storyId}/replies/count")
    public Long count(@PathVariable Long storyId) {
        return replies.countByStoryId(storyId);
    }

    // POST /api/stories/{storyId}/replies
    @PostMapping("/{storyId}/replies")
    public StoryReplyResponse add(@PathVariable Long storyId,
                                  @Valid @RequestBody CreateStoryReplyRequest request) {

        StoryReply reply = replies.addReply(storyId, request.getContent());
        return mapToResponse(reply);
    }

    // PUT /api/stories/replies/{replyId}
    @PutMapping("/replies/{replyId}")
    public StoryReplyResponse update(@PathVariable Long replyId,
                                     @Valid @RequestBody UpdateStoryReplyRequest request) {

        StoryReply reply = replies.updateReply(replyId, request.getContent());
        return mapToResponse(reply);
    }

    // DELETE /api/stories/replies/{replyId}
    @DeleteMapping("/replies/{replyId}")
    public String delete(@PathVariable Long replyId) {
        return replies.deleteReply(replyId);
    }

    private StoryReplyResponse mapToResponse(StoryReply reply) {
        return new StoryReplyResponse(
                reply.getId(),
                reply.getStory() != null ? reply.getStory().getId() : null,
                reply.getUser() != null ? reply.getUser().getId() : null,
                reply.getUser() != null ? reply.getUser().getUsername() : null,
                reply.getContent(),
                reply.getDeleted(),
                reply.getCreatedAt()
        );
    }
}
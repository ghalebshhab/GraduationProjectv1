package com.jomap.backend.Controllers.Story;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.Replies.CreateStoryReplyRequest;
import com.jomap.backend.DTOs.Stories.Replies.StoryReplyResponse;
import com.jomap.backend.DTOs.Stories.Replies.UpdateStoryReplyRequest;
import com.jomap.backend.Services.Community.Stories.Interaction.StoryRepliesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/stories")
@AllArgsConstructor
public class StoryRepliesController {

    private final StoryRepliesService replies;

    @GetMapping("/{storyId}/replies")
    public ResponseEntity<ApiResponse<List<StoryReplyResponse>>> list(@PathVariable Long storyId) {
        return ResponseEntity.ok(replies.findByStoryId(storyId));
    }

    @GetMapping("/{storyId}/replies/count")
    public ResponseEntity<ApiResponse<Long>> count(@PathVariable Long storyId) {
        return ResponseEntity.ok(replies.countByStoryId(storyId));
    }

    @PostMapping("/{storyId}/replies")
    public ResponseEntity<ApiResponse<StoryReplyResponse>> add(
            @PathVariable Long storyId,
            @Valid @RequestBody CreateStoryReplyRequest request
    ) {
        return ResponseEntity.ok(replies.addReply(storyId, request.getContent()));
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<StoryReplyResponse>> update(
            @PathVariable Long replyId,
            @Valid @RequestBody UpdateStoryReplyRequest request
    ) {
        return ResponseEntity.ok(replies.updateReply(replyId, request.getContent()));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long replyId) {
        return ResponseEntity.ok(replies.deleteReply(replyId));
    }
}
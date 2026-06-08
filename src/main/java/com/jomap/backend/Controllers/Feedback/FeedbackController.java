package com.jomap.backend.Controllers.Feedback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Feedback.FeedbackReplyRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackResponse;
import com.jomap.backend.Entities.Feedback.TargetType;
import com.jomap.backend.Services.Feedback.FeedbackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbacks(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        TargetType type;
        try {
            type = TargetType.valueOf(targetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("نوع التقييم غير مدعوم"));
        }
        return ResponseEntity.ok(feedbackService.getFeedbacks(type, targetId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> addFeedback(
            @RequestBody FeedbackRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.addFeedback(request, email));
    }

    @PostMapping("/{feedbackId}/reply")
    public ResponseEntity<ApiResponse<FeedbackResponse>> replyToFeedback(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackReplyRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.replyToFeedback(feedbackId, request, email));
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody com.jomap.backend.DTOs.Feedback.UpdateFeedbackRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackId, request, email));
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(
            @PathVariable Long feedbackId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.deleteFeedback(feedbackId, email));
    }

    @PutMapping("/{feedbackId}/reply")
    public ResponseEntity<ApiResponse<FeedbackResponse>> editReply(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackReplyRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.editReply(feedbackId, request, email));
    }

    @DeleteMapping("/{feedbackId}/reply")
    public ResponseEntity<ApiResponse<FeedbackResponse>> deleteReply(
            @PathVariable Long feedbackId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.deleteReply(feedbackId, email));
    }

    @GetMapping("/{feedbackId}/history")
    public ResponseEntity<ApiResponse<List<com.jomap.backend.DTOs.Feedback.FeedbackHistoryResponse>>> getFeedbackHistory(
            @PathVariable Long feedbackId) {
        return ResponseEntity.ok(feedbackService.getFeedbackHistory(feedbackId));
    }
}

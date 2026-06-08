package com.jomap.backend.Services.Feedback;

import java.util.List;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Feedback.FeedbackReplyRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackResponse;
import com.jomap.backend.Entities.Feedback.TargetType;

public interface FeedbackService {
    ApiResponse<List<FeedbackResponse>> getFeedbacks(TargetType targetType, Long targetId);
    ApiResponse<FeedbackResponse> addFeedback(FeedbackRequest request, String currentUserEmail);
    ApiResponse<FeedbackResponse> replyToFeedback(Long feedbackId, FeedbackReplyRequest request, String currentUserEmail);
}

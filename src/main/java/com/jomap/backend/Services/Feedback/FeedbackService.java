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
    ApiResponse<FeedbackResponse> updateFeedback(Long feedbackId, com.jomap.backend.DTOs.Feedback.UpdateFeedbackRequest request, String currentUserEmail);
    ApiResponse<Void> deleteFeedback(Long feedbackId, String currentUserEmail);
    ApiResponse<FeedbackResponse> editReply(Long feedbackId, FeedbackReplyRequest request, String currentUserEmail);
    ApiResponse<FeedbackResponse> deleteReply(Long feedbackId, String currentUserEmail);
    ApiResponse<List<com.jomap.backend.DTOs.Feedback.FeedbackHistoryResponse>> getFeedbackHistory(Long feedbackId);
}

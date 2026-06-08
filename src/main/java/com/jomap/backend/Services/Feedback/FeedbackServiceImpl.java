package com.jomap.backend.Services.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Feedback.FeedbackReplyRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackRequest;
import com.jomap.backend.DTOs.Feedback.FeedbackResponse;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Feedback.Feedback;
import com.jomap.backend.Entities.Feedback.FeedbackRepository;
import com.jomap.backend.Entities.Feedback.TargetType;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final LocationRepo locationRepo;
    private final ActivityRepository activityRepository;

    @Override
    public ApiResponse<List<FeedbackResponse>> getFeedbacks(TargetType targetType, Long targetId) {
        List<Feedback> feedbacks = feedbackRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
        List<FeedbackResponse> responseList = feedbacks.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ApiResponse.success("Fetched successfully", responseList);
    }

    @Override
    @Transactional
    public ApiResponse<FeedbackResponse> addFeedback(FeedbackRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            return ApiResponse.error("التقييم يجب أن يكون بين 1 و 5");
        }

        if (request.getTargetId() == null) {
            return ApiResponse.error("معرف المستهدف مطلوب");
        }

        if (request.getTargetType() == null || request.getTargetType().trim().isEmpty()) {
            return ApiResponse.error("نوع التقييم مطلوب");
        }

        TargetType type;
        try {
            type = TargetType.valueOf(request.getTargetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("نوع التقييم غير مدعوم");
        }

        // Validate target
        if (type == TargetType.LOCATION) {
            if (!locationRepo.existsById(request.getTargetId())) {
                return ApiResponse.error("المنشأة غير موجودة");
            }
        } else if (type == TargetType.ACTIVITY) {
            if (!activityRepository.existsById(request.getTargetId())) {
                return ApiResponse.error("الفعالية غير موجودة");
            }
        } else {
            return ApiResponse.error("نوع التقييم غير مدعوم");
        }

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setTargetId(request.getTargetId());
        feedback.setTargetType(type);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Here we could also update the Location/Activity average rating and reviewCount
        // For now we just add the feedback

        return ApiResponse.success("تم إضافة التقييم بنجاح", mapToResponse(savedFeedback));
    }

    @Override
    @Transactional
    public ApiResponse<FeedbackResponse> replyToFeedback(Long feedbackId, FeedbackReplyRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) {
            return ApiResponse.error("التقييم غير موجود");
        }

        if (feedback.getTargetType() != TargetType.LOCATION) {
            return ApiResponse.error("لا يمكن الرد إلا على تقييمات المنشآت");
        }

        LocationList location = locationRepo.findById(feedback.getTargetId()).orElse(null);
        if (location == null) {
            return ApiResponse.error("المنشأة غير موجودة");
        }

        if (!location.getOwner().getId().equals(user.getId())) {
            return ApiResponse.error("عذراً، يحق لمالك المنشأة فقط الرد على التقييمات");
        }

        feedback.setOwnerReply(request.getReplyContent());
        feedback.setRepliedAt(LocalDateTime.now());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return ApiResponse.success("تم إضافة الرد بنجاح", mapToResponse(savedFeedback));
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setUserId(feedback.getUser().getId());
        response.setUserName(feedback.getUser().getUsername());
        
        if (feedback.getUser().getProfile() != null && feedback.getUser().getProfile().getProfileImageUrl() != null) {
            response.setUserProfileImageUrl(feedback.getUser().getProfile().getProfileImageUrl());
        } else {
            response.setUserProfileImageUrl(feedback.getUser().getProfileImageUrl());
        }
        
        response.setRating(feedback.getRating());
        response.setComment(feedback.getComment());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setOwnerReply(feedback.getOwnerReply());
        response.setRepliedAt(feedback.getRepliedAt());
        
        return response;
    }
}

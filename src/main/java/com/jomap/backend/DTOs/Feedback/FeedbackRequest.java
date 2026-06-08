package com.jomap.backend.DTOs.Feedback;

import com.jomap.backend.Entities.Feedback.TargetType;
import lombok.Data;

@Data
public class FeedbackRequest {
    private Long targetId;
    private String targetType;
    private Integer rating; // 1 to 5
    private String comment;
}

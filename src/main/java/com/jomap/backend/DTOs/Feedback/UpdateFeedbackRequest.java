package com.jomap.backend.DTOs.Feedback;

import lombok.Data;

@Data
public class UpdateFeedbackRequest {
    private Integer rating; // 1 to 5
    private String comment;
}

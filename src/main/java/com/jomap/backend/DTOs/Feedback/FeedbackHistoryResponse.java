package com.jomap.backend.DTOs.Feedback;

import java.time.LocalDateTime;

import com.jomap.backend.Entities.Feedback.EditType;

import lombok.Data;

@Data
public class FeedbackHistoryResponse {
    private Long id;
    private Integer previousRating;
    private String previousComment;
    private String previousOwnerReply;
    private LocalDateTime editedAt;
    private EditType editType;
}

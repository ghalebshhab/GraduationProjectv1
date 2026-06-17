package com.jomap.backend.Services.Support;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Support.AppReviewRequest;
import com.jomap.backend.DTOs.Support.SupportTicketRequest;

public interface SupportService {
    ApiResponse<String> createTicket(SupportTicketRequest request, String userEmail);
    ApiResponse<String> submitReview(AppReviewRequest request, String userEmail);
}

package com.jomap.backend.Controllers.Support;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Support.AppReviewRequest;
import com.jomap.backend.DTOs.Support.SupportTicketRequest;
import com.jomap.backend.Services.Support.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
public class SupportController {

    private final SupportService supportService;

    // POST /api/support/tickets - يقبل مستخدم مسجل أو زائر
    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<String>> createTicket(
            @Valid @RequestBody SupportTicketRequest request,
            Authentication authentication
    ) {
        String userEmail = null;
        if (authentication != null) {
            userEmail = authentication.getName();
        }
        return ResponseEntity.ok(supportService.createTicket(request, userEmail));
    }

    // POST /api/support/reviews - مستخدم مسجل فقط
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<String>> submitReview(
            @Valid @RequestBody AppReviewRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(supportService.submitReview(request, userEmail));
    }
}

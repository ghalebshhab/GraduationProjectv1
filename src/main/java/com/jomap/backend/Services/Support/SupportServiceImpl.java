package com.jomap.backend.Services.Support;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Support.AppReviewRequest;
import com.jomap.backend.DTOs.Support.SupportTicketRequest;
import com.jomap.backend.Entities.Support.AppReview;
import com.jomap.backend.Entities.Support.AppReviewRepository;
import com.jomap.backend.Entities.Support.SupportTicket;
import com.jomap.backend.Entities.Support.SupportTicketRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final AppReviewRepository appReviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<String> createTicket(SupportTicketRequest request, String userEmail) {
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setEmail(request.getEmail());

        // If user is authenticated, link the ticket to their account
        if (userEmail != null && !userEmail.isBlank()) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                ticket.setUserId(user.getId());
                if (ticket.getEmail() == null || ticket.getEmail().isBlank()) {
                    ticket.setEmail(user.getEmail());
                }
            }
        }

        supportTicketRepository.save(ticket);
        return ApiResponse.success("تم إرسال بلاغ الدعم الفني بنجاح", "Ticket created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> submitReview(AppReviewRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        AppReview review = new AppReview();
        review.setUserId(user.getId());
        review.setRating(request.getRating());
        review.setFeedback(request.getFeedback());
        appReviewRepository.save(review);

        return ApiResponse.success("شكراً لك على تقييمك ودعمك المتواصل لتطبيق JoMap!", "Rating submitted successfully");
    }
}

package com.jomap.backend.Services.Events;

import com.jomap.backend.DTOs.Events.CreateEventRequest;
import com.jomap.backend.DTOs.Events.EventResponse;
import com.jomap.backend.DTOs.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EventService {
    ApiResponse<List<EventResponse>> getAllEventsForAdmin();

    ApiResponse<EventResponse> createEvent(CreateEventRequest request, String email);

    ApiResponse<List<EventResponse>> getMyEvents(String email);
    ApiResponse<List<EventResponse>> getApprovedEvents();

    ApiResponse<List<EventResponse>> getUpcomingApprovedEvents();

    ApiResponse<List<EventResponse>> getEventsByGovernorate(String governorate);


    ApiResponse<EventResponse> approveEvent(Long eventId);

    ApiResponse<EventResponse> rejectEvent(Long eventId);
}
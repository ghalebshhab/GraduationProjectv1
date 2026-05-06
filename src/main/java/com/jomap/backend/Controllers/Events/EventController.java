package com.jomap.backend.Controllers.Events;

import com.jomap.backend.DTOs.Events.CreateEventRequest;
import com.jomap.backend.DTOs.Events.EventResponse;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Events.EventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private final EventService eventService;


    @PostMapping
    public ApiResponse<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            return ApiResponse.error(errorMessage);
        }

        if (principal == null) {
            return ApiResponse.error("User is not authenticated");
        }

        String email = principal.getName();

        return eventService.createEvent(request, email);
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getApprovedEvents() {
        return eventService.getApprovedEvents();
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<EventResponse>> getUpcomingEvents() {
        return eventService.getUpcomingApprovedEvents();
    }

    @GetMapping("/governorate/{governorate}")
    public ApiResponse<List<EventResponse>> getEventsByGovernorate(
            @PathVariable String governorate
    ) {
        return eventService.getEventsByGovernorate(governorate);
    }

    @GetMapping("/my")
    public ApiResponse<List<EventResponse>> getMyEvents(
            Principal principal
    ) {
        if (principal == null) {
            return ApiResponse.error("User is not authenticated");
        }

        String email = principal.getName();

        return eventService.getMyEvents(email);
    }




}
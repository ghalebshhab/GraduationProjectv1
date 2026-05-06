package com.jomap.backend.Services.Events;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Events.CreateEventRequest;
import com.jomap.backend.DTOs.Events.EventResponse;
import com.jomap.backend.Entities.Events.Event;
import com.jomap.backend.Entities.Events.EventStatus;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Events.EventRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<EventResponse> createEvent(CreateEventRequest request, String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setDate(LocalDate.parse(request.getDate()));
        event.setTime(parseTime(request.getTime()));
        event.setLocationName(request.getLocationName());
        event.setGovernorate(request.getGovernorate());
        event.setImageUrl(request.getImageUrl());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setStatus(EventStatus.PENDING);
        event.setCreatedBy(user);

        Event savedEvent = eventRepository.save(event);

        return ApiResponse.success(
                "Event created successfully and waiting for admin approval",
                mapToResponse(savedEvent)
        );
    }

    @Override
    public ApiResponse<List<EventResponse>> getAllEventsForAdmin() {
        List<EventResponse> events = eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("All events fetched successfully", events);
    }

    public ApiResponse<List<EventResponse>> getApprovedEvents() {
        List<EventResponse> events = eventRepository.findByStatus(EventStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Approved events fetched successfully", events);
    }

    public ApiResponse<List<EventResponse>> getUpcomingApprovedEvents() {
        List<EventResponse> events = eventRepository.findByStatusAndDateGreaterThanEqual(
                        EventStatus.APPROVED,
                        LocalDate.now()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Upcoming events fetched successfully", events);
    }

    public ApiResponse<List<EventResponse>> getEventsByGovernorate(String governorate) {
        List<EventResponse> events = eventRepository
                .findByStatusAndGovernorate(EventStatus.APPROVED, governorate)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Events fetched successfully", events);
    }

    @Override
    public ApiResponse<List<EventResponse>> getMyEvents(String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();

        List<EventResponse> events = eventRepository.findByCreatedById(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Your events fetched successfully", events);
    }
    @Transactional
    public ApiResponse<EventResponse> approveEvent(Long eventId) {

        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isEmpty()) {
            return ApiResponse.error("Event not found");
        }

        Event event = eventOptional.get();
        event.setStatus(EventStatus.APPROVED);

        Event savedEvent = eventRepository.save(event);

        return ApiResponse.success(
                "Event approved successfully",
                mapToResponse(savedEvent)
        );
    }

    @Transactional
    public ApiResponse<EventResponse> rejectEvent(Long eventId) {

        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isEmpty()) {
            return ApiResponse.error("Event not found");
        }

        Event event = eventOptional.get();
        event.setStatus(EventStatus.REJECTED);

        Event savedEvent = eventRepository.save(event);

        return ApiResponse.success(
                "Event rejected successfully",
                mapToResponse(savedEvent)
        );
    }

    private LocalTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(time, formatter);
    }

    private EventResponse mapToResponse(Event event) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate().toString())
                .time(event.getTime().format(timeFormatter))
                .locationName(event.getLocationName())
                .governorate(event.getGovernorate())
                .imageUrl(event.getImageUrl())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .status(event.getStatus().name())
                .createdById(event.getCreatedBy().getId())
                .createdByUsername(event.getCreatedBy().getUsername())
                .build();
    }
}
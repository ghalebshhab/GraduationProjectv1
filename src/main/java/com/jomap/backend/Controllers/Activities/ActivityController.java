package com.jomap.backend.Controllers.Activities;

import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Activities.ActivityService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityController {

    @Autowired
    private final ActivityService ActivityService;

    @PostMapping("/create")
    public ApiResponse<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            BindingResult bindingResult,
            Principal principal) {

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

        return ActivityService.createActivity(request, email);
    }

    @GetMapping
    public ApiResponse<List<ActivityResponse>> getApprovedActivities() {
        return ActivityService.getApprovedActivities();
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityResponse> getActivityById(@PathVariable Long id) {
        return ActivityService.getActivityById(id);
    }

    @GetMapping("/location/{locationId}")
    public ApiResponse<List<ActivityResponse>> getActivitiesByLocation(@PathVariable Long locationId) {
        return ActivityService.getActivitiesByLocation(locationId);
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<ActivityResponse>> getUpcomingActivities() {
        return ActivityService.getUpcomingApprovedActivities();
    }

    @GetMapping("/governorate/{governorateId}")
    public ApiResponse<List<ActivityResponse>> getActivitiesByGovernorate(
            @PathVariable Long governorateId) {
        return ActivityService.getActivitiesByGovernorate(governorateId);
    }

    @GetMapping("/my")
    public ApiResponse<List<ActivityResponse>> getMyActivities(
            Principal principal) {
        if (principal == null) {
            return ApiResponse.error("User is not authenticated");
        }

        String email = principal.getName();

        return ActivityService.getMyActivities(email);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<ActivityResponse> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody com.jomap.backend.DTOs.Activities.UpdateActivityRequest request,
            BindingResult bindingResult,
            Principal principal) {



        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            return ApiResponse.error(errorMessage);
        }

        if (principal == null) {
            return ApiResponse.error("المستخدم غير موثق بالأنظمة");
        }

        String email = principal.getName();

        return ActivityService.updateActivity(id, request, email);
    }

    @PutMapping("/cancel/{id}")
    public ApiResponse<ActivityResponse> cancelActivity(
            @PathVariable Long id,
            Principal principal) {
        
        if (principal == null) {
            return ApiResponse.error("المستخدم غير موثق بالأنظمة");
        }

        return ActivityService.cancelActivity(id);
    }

    @PostMapping("/{id}/register")
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> registerForActivity(
            @PathVariable Long id,
            @RequestBody(required = false) com.jomap.backend.DTOs.Activities.RegisterActivityRequest request,
            Principal principal) {
        if (principal == null) {
            return ApiResponse.error("المستخدم غير موثق بالأنظمة");
        }
        
        return ActivityService.registerForActivity(id, principal.getName(), request);
    }

    @GetMapping("/{id}/registrations")
    public ApiResponse<List<com.jomap.backend.DTOs.Activities.RegistrationResponse>> getActivityRegistrations(
            @PathVariable Long id) {
        return ActivityService.getActivityRegistrations(id);
    }

    @PostMapping("/registrations/{regId}/status")
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> updateRegistrationStatus(
            @PathVariable Long regId,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) java.util.Map<String, String> body) {
            
        String finalStatus = status;
        if (finalStatus == null && body != null && body.containsKey("status")) {
            finalStatus = body.get("status");
        }
        
        if (finalStatus == null) {
            return ApiResponse.error("الحالة مطلوبة");
        }
        
        return ActivityService.updateRegistrationStatus(regId, finalStatus);
    }

    @GetMapping("/{id}/my-registration")
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> getMyRegistration(
            @PathVariable Long id,
            Principal principal) {
        if (principal == null) {
            return ApiResponse.error("المستخدم غير موثق بالأنظمة");
        }
        return ActivityService.getMyRegistration(id, principal.getName());
    }
}
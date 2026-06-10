package com.jomap.backend.Services.Activities;

import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.ApiResponse;

import java.util.List;

public interface ActivityService {
    
    ApiResponse<List<ActivityResponse>> getAllActivitiesForAdmin();

    ApiResponse<ActivityResponse> getActivityById(Long activityId);

    ApiResponse<ActivityResponse> createActivity(CreateActivityRequest request, String email);

    ApiResponse<List<ActivityResponse>> getMyActivities(String email);

    ApiResponse<List<ActivityResponse>> getApprovedActivities();

    ApiResponse<List<ActivityResponse>> getUpcomingApprovedActivities();

    ApiResponse<List<ActivityResponse>> getActivitiesByGovernorate(Long governorateId);

    ApiResponse<ActivityResponse> approveActivity(Long activityId);

    ApiResponse<ActivityResponse> rejectActivity(Long activityId);

    ApiResponse<ActivityResponse> postponeActivity(Long activityId);

    ApiResponse<ActivityResponse> cancelActivity(Long activityId);

    ApiResponse<List<ActivityResponse>> getCompletedActivities();

    ApiResponse<ActivityResponse> updateActivity(Long activityId, com.jomap.backend.DTOs.Activities.UpdateActivityRequest request, String ownerEmail);

    ApiResponse<List<ActivityResponse>> getActivitiesByLocation(Long locationId);

    ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> registerForActivity(Long activityId, String email, com.jomap.backend.DTOs.Activities.RegisterActivityRequest request);

    ApiResponse<List<com.jomap.backend.DTOs.Activities.RegistrationResponse>> getActivityRegistrations(Long activityId);

    ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> updateRegistrationStatus(Long registrationId, String status);

    ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> getMyRegistration(Long activityId, String email);
}
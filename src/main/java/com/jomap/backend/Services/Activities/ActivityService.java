package com.jomap.backend.Services.Activities;

import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.ApiResponse;

public interface ActivityService {
    // ميثود إنشاء الفعالية اللي رح ترجع ApiResponse موحد
    ApiResponse<ActivityResponse> createActivity(CreateActivityRequest request);
}
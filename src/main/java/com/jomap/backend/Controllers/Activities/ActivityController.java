package com.jomap.backend.Controllers.Activities;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Activities.ActivityService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping("/create")
    public ApiResponse<ActivityResponse> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return activityService.createActivity(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // بنطول أول خطأ صار (مثل: "عنوان الفعالية مطلوب")
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ApiResponse.error(errorMessage);
    }
}
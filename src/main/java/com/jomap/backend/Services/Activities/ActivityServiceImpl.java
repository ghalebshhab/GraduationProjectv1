package com.jomap.backend.Services.Activities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Gove.GovernorateRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> createActivity(CreateActivityRequest request, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("فشل الإنشاء: المستخدم غير موجود في النظام");
        }
        User user = userOptional.get();

        Optional<Governorate> optionalGov = governorateRepository.findById(request.getGovernorateId());
        if (optionalGov.isEmpty()) {
            return ApiResponse.error("العملية مرفوضة: المحافظة المحددة غير مدعومة حالياً");
        }

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setDate(request.getDate());
        activity.setTime(request.getTime());
        activity.setActivityLocation(request.getActivityLocation());
        activity.setGovernorate(optionalGov.get());
        activity.setImageUrl(request.getImageUrl());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());
        activity.setStatus(ActivityStatus.PENDING);
        activity.setCreatedBy(user);
        activity.setPrice(request.getPrice());
        activity.setAttendeesCount(request.getAttendeesCount());

        Activity savedActivity = activityRepository.save(activity);
        return ApiResponse.success("تم إنشاء النشاط بنجاح وهو بانتظار موافقة المسؤول", mapToResponse(savedActivity));
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getMyActivities(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("لا يمكن جلب البيانات: المستخدم غير موثق");
        }

        List<ActivityResponse> activities = activityRepository.findByCreatedById(userOptional.get().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("تم جلب نشاطاتك بنجاح", activities);
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getApprovedActivities() {
        List<ActivityResponse> activities = activityRepository.findByStatus(ActivityStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Approved Activities fetched successfully", activities);
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getUpcomingApprovedActivities() {
        List<ActivityResponse> activities = activityRepository
                .findByStatusAndDateGreaterThanEqual(ActivityStatus.APPROVED, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Upcoming Activities fetched successfully", activities);
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getActivitiesByGovernorate(Long governorateId) {
        List<ActivityResponse> activities = activityRepository
                .findByStatusAndGovernorateId(ActivityStatus.APPROVED, governorateId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("تم جلب نشاطات المحافظة", activities);
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getCompletedActivities() {
        List<ActivityResponse> activities = activityRepository.findByStatus(ActivityStatus.COMPLETED)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("تم جلب الأنشطة المكتملة", activities);
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getAllActivitiesForAdmin() {
        List<ActivityResponse> activities = activityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Fetched All", activities);
    }

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> approveActivity(Long activityId) {
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            return ApiResponse.error("النشاط غير موجود");
        }
        Activity activity = activityOptional.get();
        activity.setStatus(ActivityStatus.APPROVED);
        return ApiResponse.success("تمت الموافقة على النشاط", mapToResponse(activityRepository.save(activity)));
    }

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> rejectActivity(Long activityId) {
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty())
            return ApiResponse.error("النشاط غير موجود");
        Activity activity = activityOptional.get();
        activity.setStatus(ActivityStatus.REJECTED);
        return ApiResponse.success("تم رفض النشاط", mapToResponse(activityRepository.save(activity)));
    }

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> cancelActivity(Long activityId) {
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty())
            return ApiResponse.error("النشاط غير موجود");
        Activity activity = activityOptional.get();
        activity.setStatus(ActivityStatus.CANCELLED);
        return ApiResponse.success("تم إلغاء النشاط بنجاح", mapToResponse(activityRepository.save(activity)));
    }

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> postponeActivity(Long activityId) {
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty())
            return ApiResponse.error("النشاط غير موجود");
        Activity activity = activityOptional.get();
        activity.setStatus(ActivityStatus.POSTPONED);
        return ApiResponse.success("تم تأجيل النشاط", mapToResponse(activityRepository.save(activity)));
    }

    private LocalTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(time, formatter);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .date(activity.getDate().toString())
                .time(activity.getTime())
                .activityLocation(activity.getActivityLocation())
                .governorateId(activity.getGovernorate().getId())
                .imageUrl(activity.getImageUrl())
                .latitude(activity.getLatitude())
                .price(activity.getPrice())
                .attendeesCount(activity.getAttendeesCount())
                .longitude(activity.getLongitude())
                .statusId((long) activity.getStatus().getId())
                .createdById(activity.getCreatedBy().getId())
                .createdByUsername(activity.getCreatedBy().getUsername())
                .build();
    }
}
package com.jomap.backend.Services.Activities;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Activities.ActivitySchedule;
import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Governorate.GovernorateRepository;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Notifications.Notification;
import com.jomap.backend.Entities.Notifications.NotificationCategory;
import com.jomap.backend.Entities.Notifications.NotificationRepository;
import com.jomap.backend.Entities.Notifications.NotificationType;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final PostRepository postRepository;
    private final LocationRepo locationRepo;
    private final com.jomap.backend.Entities.Activities.RegistrationRepository registrationRepository;
    private final com.jomap.backend.Entities.Feedback.FeedbackRepository feedbackRepository;
    private final com.jomap.backend.Services.Notefications.EmailService emailService;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public ApiResponse<ActivityResponse> createActivity(CreateActivityRequest request, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("فشل الإنشاء: المستخدم غير موجود في النظام");
        }
        User user = userOptional.get();

        if (request.getSchedules() != null) {
            for (ActivitySchedule schedule : request.getSchedules()) {
                try {
                    LocalTime sTime = parseTime(schedule.getStartTime());
                    LocalTime eTime = parseTime(schedule.getEndTime());
                    if (!eTime.isAfter(sTime)) {
                        return ApiResponse.error("في نفس اليوم، يجب أن يكون وقت النهاية بعد وقت البداية");
                    }
                } catch (Exception e) {
                    return ApiResponse.error("صيغة الوقت غير صحيحة، الرجاء التأكد من الإدخال");
                }
            }
        }

        Optional<Governorate> optionalGov = governorateRepository.findById(request.getGovernorateId());
        if (optionalGov.isEmpty()) {
            return ApiResponse.error("العملية مرفوضة: المحافظة المحددة غير مدعومة حالياً");
        }

        if (request.getLocationId() != null) {
            Optional<com.jomap.backend.Entities.Locations.LocationList> locOpt = locationRepo.findById(request.getLocationId());
            if (locOpt.isEmpty()) {
                return ApiResponse.error("المنشأة غير موجودة");
            }
            if (locOpt.get().getStatus() != com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED) {
                return ApiResponse.error("عذراً، يجب أن تكون حالة المنشأة منشورة (PUBLISHED) لتتمكن من إضافة فعالية");
            }
        }

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setActivityLocation(request.getActivityLocation());
        activity.setLocationId(request.getLocationId());
        activity.setGovernorate(optionalGov.get());
        activity.setImageUrl(request.getImageUrl());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());
        activity.setStatus(ActivityStatus.PENDING);
        activity.setCreatedBy(user);
        activity.setPrice(request.getPrice());
        activity.setMaxCapacity(request.getMaxCapacity());
        activity.setAttendeesCount(request.getAttendeesCount());

        activity.setScheduleType(request.getScheduleType());
        activity.setTotalActualDays(request.getTotalActualDays());

        List<com.jomap.backend.Entities.Activities.ActivitySchedule> schedules = new ArrayList<>();
        if (request.getSchedules() != null) {
            for (ActivitySchedule dto : request.getSchedules()) {
                com.jomap.backend.Entities.Activities.ActivitySchedule schedule = new com.jomap.backend.Entities.Activities.ActivitySchedule();
                schedule.setDate(dto.getDate());
                schedule.setDayName(dto.getDayName());
                schedule.setStartTime(dto.getStartTime());
                schedule.setEndTime(dto.getEndTime());
                schedule.setActivity(activity);
                schedules.add(schedule);
            }
        }
        activity.setSchedules(schedules);

        Activity savedActivity = activityRepository.save(activity);

        String postContent = "New Activity: " + (savedActivity.getTitle() != null ? savedActivity.getTitle() : "") 
                           + "\n" + (savedActivity.getDescription() != null ? savedActivity.getDescription() : "");
        if (postContent.length() > 2000) {
            postContent = postContent.substring(0, 1997) + "...";
        }

        Post activityPost = new Post(
                user,
                postContent,
                savedActivity.getImageUrl(),
                Post.PostType.ACTIVITY
        );
        activityPost.setLatitude(savedActivity.getLatitude());
        activityPost.setLongitude(savedActivity.getLongitude());
        activityPost.setCategory("ACTIVITY");
        activityPost.setActivityId(savedActivity.getId());
        
        postRepository.save(activityPost);

        return ApiResponse.success("تم إنشاء النشاط بنجاح وهو بانتظار موافقة المسؤول", mapToResponse(savedActivity));
    }

    @Override
    public ApiResponse<ActivityResponse> getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .map(activity -> ApiResponse.success("تم جلب تفاصيل الفعالية بنجاح", mapToResponse(activity)))
                .orElse(ApiResponse.error("الفعالية غير موجودة"));
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
    public ApiResponse<com.jomap.backend.DTOs.PaginatedResponse<ActivityResponse>> getApprovedActivities(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Activity> activityPage = activityRepository.findByStatusInOrderByIdDesc(
                List.of(ActivityStatus.APPROVED, ActivityStatus.POSTPONED), pageable);
        
        org.springframework.data.domain.Page<ActivityResponse> responsePage = activityPage.map(this::mapToResponse);
        return ApiResponse.success("Approved Activities fetched successfully", com.jomap.backend.DTOs.PaginatedResponse.from(responsePage));
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getUpcomingApprovedActivities() {
        List<ActivityResponse> activities = activityRepository.findByStatus(ActivityStatus.APPROVED)
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
        
        // Notify users
        List<com.jomap.backend.Entities.Activities.Registration> registrations = registrationRepository.findByActivityId(activityId);
        if (registrations != null && !registrations.isEmpty()) {
            for (com.jomap.backend.Entities.Activities.Registration reg : registrations) {
                if (reg.getUser() != null && reg.getUser().getEmail() != null) {
                    emailService.sendActivityStatusNotification(reg.getUser().getEmail(), activity.getTitle(), "ملغية", "نأسف لإعلامكم بأنه تم إلغاء هذه الفعالية.");
                }
            }
        }
        
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
        
        // Notify users
        List<com.jomap.backend.Entities.Activities.Registration> registrations = registrationRepository.findByActivityId(activityId);
        if (registrations != null && !registrations.isEmpty()) {
            for (com.jomap.backend.Entities.Activities.Registration reg : registrations) {
                if (reg.getUser() != null && reg.getUser().getEmail() != null) {
                    emailService.sendActivityStatusNotification(reg.getUser().getEmail(), activity.getTitle(), "مؤجلة", "نود إعلامكم بأنه تم تأجيل هذه الفعالية وتعديل مواعيدها، يرجى التحقق من المواعيد الجديدة من خلال التطبيق.");
                }
            }
        }
        
        return ApiResponse.success("تم تأجيل النشاط", mapToResponse(activityRepository.save(activity)));
    }


    @Override
    @Transactional
    public ApiResponse<ActivityResponse> updateActivity(Long activityId, com.jomap.backend.DTOs.Activities.UpdateActivityRequest request, String ownerEmail) {
        if (request.getSchedules() != null) {
            for (com.jomap.backend.DTOs.Activities.ActivitySchedule schedule : request.getSchedules()) {
                try {
                    LocalTime sTime = parseTime(schedule.getStartTime());
                    LocalTime eTime = parseTime(schedule.getEndTime());
                    if (!eTime.isAfter(sTime)) {
                        return ApiResponse.error("في نفس اليوم، يجب أن يكون وقت النهاية بعد وقت البداية");
                    }
                } catch (Exception e) {
                    return ApiResponse.error("صيغة الوقت غير صحيحة، الرجاء التأكد من الإدخال");
                }
            }
        }

        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            return ApiResponse.error("فشل التحديث: الفعالية غير موجودة");
        }
        Activity activity = activityOptional.get();

        if (!activity.getCreatedBy().getEmail().equals(ownerEmail)) {
            return ApiResponse.error("العملية مرفوضة: ليس لديك صلاحية لتعديل هذه الفعالية");
        }

        if (activity.getStatus() == ActivityStatus.REJECTED || 
            activity.getStatus() == ActivityStatus.CANCELLED || 
            activity.getStatus() == ActivityStatus.COMPLETED) {
            return ApiResponse.error("العملية حظرت: لا يمكن تعديل فعالية مرفوضة، ملغاة، أو منتهية ومكتملة");
        }

        Optional<Governorate> optionalGov = governorateRepository.findById(request.getGovernorateId());
        if (optionalGov.isEmpty()) {
            return ApiResponse.error("فشل التحديث: المحافظة المحددة غير مدعومة");
        }

        boolean isScheduleChanged = false;
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            if (activity.getSchedules().size() != request.getSchedules().size()) {
                isScheduleChanged = true;
            } else {
                for (int i = 0; i < request.getSchedules().size(); i++) {
                    ActivitySchedule newSched = request.getSchedules().get(i);
                    com.jomap.backend.Entities.Activities.ActivitySchedule oldSched = activity.getSchedules().get(i);
                    if (!newSched.getDate().equals(oldSched.getDate()) ||
                        !newSched.getStartTime().equals(oldSched.getStartTime()) ||
                        !newSched.getEndTime().equals(oldSched.getEndTime())) {
                        isScheduleChanged = true;
                        break;
                    }
                }
            }
        }

        if (isScheduleChanged && activity.getStatus() == ActivityStatus.APPROVED) {
            activity.setStatus(ActivityStatus.POSTPONED);
        }

        java.util.List<String> changes = new java.util.ArrayList<>();
        if (!java.util.Objects.equals(activity.getTitle(), request.getTitle())) changes.add("عنوان الفعالية");
        if (!java.util.Objects.equals(activity.getDescription(), request.getDescription())) changes.add("وصف الفعالية");
        if (!java.util.Objects.equals(activity.getActivityLocation(), request.getActivityLocation())) changes.add("موقع الفعالية");
        if (!java.util.Objects.equals(activity.getLocationId(), request.getLocationId())) changes.add("معرف المنشأة");
        if (!activity.getGovernorate().getId().equals(request.getGovernorateId())) changes.add("المحافظة");

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setActivityLocation(request.getActivityLocation());
        activity.setLocationId(request.getLocationId());
        activity.setGovernorate(optionalGov.get());
        
        Double oldPrice = activity.getPrice();
        Double newPrice = request.getPrice();
        if ((oldPrice == null && newPrice != null) || 
            (oldPrice != null && newPrice == null) || 
            (oldPrice != null && newPrice != null && !oldPrice.equals(newPrice))) {
            activity.setOldPrice(oldPrice == null ? 0.0 : oldPrice);
            changes.add("سعر الفعالية");
        }
        activity.setPrice(newPrice);
        
        if (!java.util.Objects.equals(activity.getMaxCapacity(), request.getMaxCapacity())) changes.add("سعة الفعالية");
        activity.setMaxCapacity(request.getMaxCapacity());
        activity.setScheduleType(request.getScheduleType());
        activity.setTotalActualDays(request.getTotalActualDays());
        
        if (request.getImageUrl() != null && !request.getImageUrl().equals(activity.getImageUrl())) {
            activity.setImageUrl(request.getImageUrl());
            changes.add("بوستر أو صورة الفعالية");
        } else if (request.getImageUrl() != null) {
            activity.setImageUrl(request.getImageUrl());
        }
        
        if (request.getLatitude() != null) activity.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) activity.setLongitude(request.getLongitude());

        if (isScheduleChanged) changes.add("المواعيد والتواريخ");

        // إعادة بناء لستة المواعيد الجديدة
        activity.getSchedules().clear();
        if (request.getSchedules() != null) {
            for (ActivitySchedule dto : request.getSchedules()) {
                com.jomap.backend.Entities.Activities.ActivitySchedule schedule = new com.jomap.backend.Entities.Activities.ActivitySchedule();
                schedule.setDate(dto.getDate());
                schedule.setDayName(dto.getDayName());
                schedule.setStartTime(dto.getStartTime());
                schedule.setEndTime(dto.getEndTime());
                schedule.setActivity(activity); 
                activity.getSchedules().add(schedule);
            }
        }

        Activity updatedActivity = activityRepository.save(activity);
        
        if (!changes.isEmpty()) {
            List<com.jomap.backend.Entities.Activities.Registration> registrations = registrationRepository.findByActivityId(activityId);
            if (registrations != null && !registrations.isEmpty()) {
                for (com.jomap.backend.Entities.Activities.Registration reg : registrations) {
                    if (reg.getUser() != null && reg.getUser().getEmail() != null) {
                        emailService.sendActivityDetailedUpdateNotification(reg.getUser().getEmail(), activity.getTitle(), changes);
                    }
                }
            }
        }
        
        String successMessage = (activity.getStatus() == ActivityStatus.POSTPONED) ? 
            "تم تأجيل الفعالية تلقائياً لتغيير المواعيد، وبانتظار بت المسؤول مجدداً" : 
            "تم تحديث معلومات الفعالية بنجاح، وسيتم إشعار المسجلين";

        return ApiResponse.success(successMessage, mapToResponse(updatedActivity));
    }


    private LocalTime parseTime(String time) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            return LocalTime.parse(time.trim(), formatter);
        } catch (Exception e) {
            return LocalTime.parse(time.trim());
        }
    }

    private java.time.LocalDateTime parseScheduleDateTime(String dateStr, String timeStr) {
        java.time.LocalDate date;
        try {
            date = java.time.LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            try {
                date = java.time.LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e2) {
                return java.time.LocalDateTime.MAX;
            }
        }
        LocalTime time = parseTime(timeStr);
        return java.time.LocalDateTime.of(date, time);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        List<ActivitySchedule> scheduleDtos = new ArrayList<>();
        if (activity.getSchedules() != null) {
            for (com.jomap.backend.Entities.Activities.ActivitySchedule schedule : activity.getSchedules()) {
                scheduleDtos.add(ActivitySchedule.builder()
                        .date(schedule.getDate())
                        .dayName(schedule.getDayName())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .build());
            }
        }

        String locationPhone = null;
        String locationEmail = null;
        String locationName = null;
        if (activity.getCreatedBy() != null) {
            Optional<LocationList> locationOpt = locationRepo.findByOwnerId(activity.getCreatedBy().getId());
            if (locationOpt.isPresent()) {
                locationPhone = locationOpt.get().getPhoneNumber();
                locationEmail = locationOpt.get().getEmail();
                locationName = locationOpt.get().getName();
            } else {
                locationPhone = activity.getCreatedBy().getPhoneNumber();
                locationEmail = activity.getCreatedBy().getEmail();
            }
        }

        int actualAttendeesCount = registrationRepository.countByActivityIdAndStatus(activity.getId(), com.jomap.backend.Entities.Activities.RegistrationStatus.APPROVED);

        boolean isFavorite = false;
        String registrationStatus = null;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    isFavorite = user.getFavoriteEvents().stream().anyMatch(a -> a.getId().equals(activity.getId()));
                    Optional<com.jomap.backend.Entities.Activities.Registration> regOpt = registrationRepository.findByActivityIdAndUserId(activity.getId(), user.getId());
                    if (regOpt.isPresent()) {
                        registrationStatus = regOpt.get().getStatus().name();
                    }
                }
            }
        } catch (Exception ignored) { }

        List<com.jomap.backend.Entities.Feedback.Feedback> feedbacks = feedbackRepository.findByTargetTypeAndTargetIdAndIsDeletedFalseOrderByCreatedAtDesc(
                com.jomap.backend.Entities.Feedback.TargetType.ACTIVITY, activity.getId());
        double averageRating = 0.0;
        if (!feedbacks.isEmpty()) {
            averageRating = feedbacks.stream().mapToInt(com.jomap.backend.Entities.Feedback.Feedback::getRating).average().orElse(0.0);
        }

        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .activityLocation(activity.getActivityLocation())
                .locationId(activity.getLocationId())
                .governorateId(activity.getGovernorate().getId())
                .governorateName(activity.getGovernorate().getName())
                .imageUrl(activity.getImageUrl())
                .latitude(activity.getLatitude())
                .price(activity.getPrice())
                .oldPrice(activity.getOldPrice())
                .maxCapacity(activity.getMaxCapacity() != null ? activity.getMaxCapacity() : 0)
                .attendeesCount(actualAttendeesCount)
                .longitude(activity.getLongitude())
                .statusId((long) activity.getStatus().getId())
                .createdById(activity.getCreatedBy() != null ? activity.getCreatedBy().getId() : null)
                .createdByUsername(activity.getCreatedBy() != null ? activity.getCreatedBy().getUsername() : null)
                .scheduleType(activity.getScheduleType())
                .totalActualDays(activity.getTotalActualDays())
                .schedules(scheduleDtos)
                .locationPhone(locationPhone)
                .locationEmail(locationEmail)
                .locationName(locationName)
                .isFavorite(isFavorite)
                .registrationStatus(registrationStatus)
                .averageRating(averageRating)
                .reviewCount(feedbacks.size())
                .build();
    }
    
    @Override
    public ApiResponse<List<ActivityResponse>> getActivitiesByLocation(Long locationId) {
        Optional<LocationList> locationOptional = locationRepo.findById(locationId);
        if (locationOptional.isEmpty() || locationOptional.get().getOwner() == null) {
            return ApiResponse.error("المنشأة غير موجودة");
        }
        Long ownerId = locationOptional.get().getOwner().getId();
        
        List<ActivityResponse> activities = activityRepository.findByCreatedById(ownerId)
                .stream()
                .filter(a -> a.getStatus() == ActivityStatus.APPROVED ||
                             a.getStatus() == ActivityStatus.COMPLETED ||
                             a.getStatus() == ActivityStatus.POSTPONED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
                
        return ApiResponse.success("تم جلب الأنشطة بنجاح", activities);
    }

    @Override
    @Transactional
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> registerForActivity(Long activityId, String email, com.jomap.backend.DTOs.Activities.RegisterActivityRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }
        User user = userOptional.get();

        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            return ApiResponse.error("الفعالية غير موجودة");
        }
        Activity activity = activityOptional.get();

        if (activity.getSchedules() != null && !activity.getSchedules().isEmpty()) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime earliestSchedule = activity.getSchedules().stream()
                .map(s -> parseScheduleDateTime(s.getDate(), s.getStartTime()))
                .min(java.time.LocalDateTime::compareTo)
                .orElse(java.time.LocalDateTime.MAX);

            if (now.isAfter(earliestSchedule)) {
                return ApiResponse.error("عذراً، لا يمكن التسجيل لأن الفعالية قد بدأت بالفعل");
            }
        }

        if (registrationRepository.existsByActivityIdAndUserId(activityId, user.getId())) {
            return ApiResponse.error("لقد قمت بالتسجيل في هذه الفعالية مسبقاً");
        }

        com.jomap.backend.Entities.Activities.Registration registration = new com.jomap.backend.Entities.Activities.Registration();
        registration.setActivity(activity);
        registration.setUser(user);
        registration.setStatus(com.jomap.backend.Entities.Activities.RegistrationStatus.PENDING);
        if (request != null) {
            registration.setGovernorateId(request.getGovernorateId());
            registration.setDetailedAddress(request.getDetailedAddress());
        }
        
        com.jomap.backend.Entities.Activities.Registration savedRegistration = registrationRepository.save(registration);

        if (activity.getCreatedBy() != null && !activity.getCreatedBy().getId().equals(user.getId())) {
            String username = user.getUsername() != null ? user.getUsername() : "مستخدم جديد";
            if (user.getProfile() != null && user.getProfile().getFirstName() != null) {
                username = user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
            }
            Notification notification = Notification.builder()
                    .text(username + " سجل في الفعالية: " + activity.getTitle())
                    .type(NotificationType.REGISTER)
                    .category(NotificationCategory.ACTIVITY)
                    .toUser(activity.getCreatedBy())
                    .fromUser(user)
                    .activityId(activity.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        // Notification for the user who registered
        Notification userNotification = Notification.builder()
                .text("تم استلام طلب تسجيلك في الفعالية: " + activity.getTitle())
                .type(NotificationType.ACTIVITY_REGISTRATION)
                .category(NotificationCategory.USER)
                .toUser(user)
                .activityId(activity.getId())
                .isRead(false)
                .build();
        notificationRepository.save(userNotification);

        return ApiResponse.success("تم إرسال طلب التسجيل بنجاح", mapToRegistrationResponse(savedRegistration));
    }

    @Override
    public ApiResponse<List<com.jomap.backend.DTOs.Activities.RegistrationResponse>> getActivityRegistrations(Long activityId) {
        List<com.jomap.backend.DTOs.Activities.RegistrationResponse> registrations = registrationRepository.findByActivityId(activityId)
                .stream()
                .map(this::mapToRegistrationResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("تم جلب المسجلين بنجاح", registrations);
    }

    @Override
    @Transactional
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> updateRegistrationStatus(Long registrationId, String statusStr, String currentUserEmail) {
        Optional<com.jomap.backend.Entities.Activities.Registration> registrationOptional = registrationRepository.findById(registrationId);
        if (registrationOptional.isEmpty()) {
            return ApiResponse.error("طلب التسجيل غير موجود");
        }
        com.jomap.backend.Entities.Activities.Registration registration = registrationOptional.get();

        com.jomap.backend.Entities.Activities.RegistrationStatus status = null;
        try {
            // First check if it's a numeric ID
            int statusId = Integer.parseInt(statusStr);
            for (com.jomap.backend.Entities.Activities.RegistrationStatus s : com.jomap.backend.Entities.Activities.RegistrationStatus.values()) {
                if (s.getId() == statusId) {
                    status = s;
                    break;
                }
            }
            if (status == null) {
                return ApiResponse.error("معرف الحالة غير صالح");
            }
        } catch (NumberFormatException e) {
            // Not a number, try parsing as String Enum
            try {
                status = com.jomap.backend.Entities.Activities.RegistrationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("حالة غير صالحة");
            }
        }

        User currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        boolean isActivityOwner = registration.getActivity().getCreatedBy().getId().equals(currentUser.getId());
        boolean isRegistrationOwner = registration.getUser().getId().equals(currentUser.getId());

        if (!isActivityOwner && !isRegistrationOwner) {
            return ApiResponse.error("لا تملك صلاحية لتعديل هذا الطلب");
        }

        if (isRegistrationOwner && !isActivityOwner) {
            // User can only withdraw
            if (status != com.jomap.backend.Entities.Activities.RegistrationStatus.WITHDRAWN) {
                return ApiResponse.error("المستخدم يمكنه فقط الانسحاب من الفعالية");
            }
        }

        if (isActivityOwner && !isRegistrationOwner) {
            // Owner cannot withdraw
            if (status == com.jomap.backend.Entities.Activities.RegistrationStatus.WITHDRAWN) {
                return ApiResponse.error("صاحب الفعالية لا يمكنه تعيين حالة انسحاب");
            }
        }

        registration.setStatus(status);
        com.jomap.backend.Entities.Activities.Registration savedRegistration = registrationRepository.save(registration);

        // Send email based on status change
        if (status == com.jomap.backend.Entities.Activities.RegistrationStatus.APPROVED) {
            String customMessage = "تم قبول طلب تسجيلك بنجاح. شكراً لك.";
            emailService.sendActivityStatusNotification(registration.getUser().getEmail(), registration.getActivity().getTitle(), status.getLabel(), customMessage);
            
            try {
                Notification dbNotification = Notification.builder()
                        .text("تم قبول تسجيلك في الفعالية: " + registration.getActivity().getTitle())
                        .type(NotificationType.REGISTRATION_ACCEPTED)
                        .category(NotificationCategory.USER)
                        .toUser(registration.getUser())
                        .fromUser(currentUser)
                        .activityId(registration.getActivity().getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(dbNotification);
            } catch (Exception e) {
                System.out.println("Failed to save activity registration approval notification: " + e.getMessage());
            }
        } else if (status == com.jomap.backend.Entities.Activities.RegistrationStatus.REJECTED) {
            String customMessage = "نأسف، تم رفض طلب تسجيلك في هذه الفعالية.";
            emailService.sendActivityStatusNotification(registration.getUser().getEmail(), registration.getActivity().getTitle(), status.getLabel(), customMessage);
            
            try {
                Notification dbNotification = Notification.builder()
                        .text("تم رفض تسجيلك في الفعالية: " + registration.getActivity().getTitle())
                        .type(NotificationType.REGISTRATION_REJECTED)
                        .category(NotificationCategory.USER)
                        .toUser(registration.getUser())
                        .fromUser(currentUser)
                        .activityId(registration.getActivity().getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(dbNotification);
            } catch (Exception e) {
                System.out.println("Failed to save activity registration rejection notification: " + e.getMessage());
            }
        } else if (status == com.jomap.backend.Entities.Activities.RegistrationStatus.CANCELLED) {
            String customMessage = "تم إلغاء طلب تسجيلك في هذه الفعالية من قبل المنظم.";
            emailService.sendActivityStatusNotification(registration.getUser().getEmail(), registration.getActivity().getTitle(), status.getLabel(), customMessage);
        }

        return ApiResponse.success("تم تحديث حالة التسجيل بنجاح", mapToRegistrationResponse(savedRegistration));
    }

    private com.jomap.backend.DTOs.Activities.RegistrationResponse mapToRegistrationResponse(com.jomap.backend.Entities.Activities.Registration registration) {
        User user = registration.getUser();
        String fullName = user.getUsername(); 
        if (user.getProfile() != null) {
            fullName = user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = registration.getRegistrationTime() != null ? registration.getRegistrationTime().format(timeFormatter) : null;
        String formattedDate = registration.getRegistrationDate() != null ? registration.getRegistrationDate().toString() : null;

        return com.jomap.backend.DTOs.Activities.RegistrationResponse.builder()
                .id(registration.getId())
                .userId(user.getId())
                .fullName(fullName != null ? fullName : "")
                .userEmail(user.getEmail() != null ? user.getEmail() : "")
                .username(user.getUsername() != null ? user.getUsername() : "")
                .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                .statusId((long) registration.getStatus().getId())
                .registrationDate(formattedDate)
                .registrationTime(formattedTime)
                .userImageUrl(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : (user.getProfile() != null ? user.getProfile().getProfileImageUrl() : null))
                .build();
    }

    @Override
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> getMyRegistration(Long activityId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }
        
        Optional<com.jomap.backend.Entities.Activities.Registration> regOpt = registrationRepository.findByActivityIdAndUserId(activityId, userOptional.get().getId());
        if (regOpt.isEmpty()) {
            return ApiResponse.error("لم يتم العثور على تسجيل");
        }
        
        return ApiResponse.success("تم جلب التسجيل", mapToRegistrationResponse(regOpt.get()));
    }

    @Override
    @Transactional
    public ApiResponse<String> toggleFavoriteActivity(Long activityId, String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ApiResponse.error("User not found");

        User user = userOptional.get();
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return ApiResponse.error("Activity not found");

        boolean isFavorited = user.getFavoriteEvents().stream().anyMatch(a -> a.getId().equals(activityId));
        if (isFavorited) {
            user.getFavoriteEvents().removeIf(a -> a.getId().equals(activityId));
            userRepository.save(user);
            return ApiResponse.success("تم الإزالة من المحفوظات", null);
        } else {
            user.getFavoriteEvents().add(activity);
            userRepository.save(user);
            return ApiResponse.success("تم الإضافة إلى المحفوظات بنجاح", null);
        }
    }

    @Override
    public ApiResponse<List<ActivityResponse>> getFavoriteActivities(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ApiResponse.error("User not found");

        User user = userOptional.get();
        List<ActivityResponse> responses = user.getFavoriteEvents().stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Favorite activities fetched", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<com.jomap.backend.DTOs.Notifications.NotificationResponse>> getActivityNotifications(Long activityId, String email) {
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return ApiResponse.error("Activity not found");
        }

        boolean isOwner = activity.getCreatedBy() != null && activity.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole() == com.jomap.backend.Entities.Users.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            return ApiResponse.error("غير مصرح لك بعرض إشعارات هذه الفعالية");
        }

        List<Notification> notifications = notificationRepository.findByActivityIdOrderByCreatedAtDesc(activityId);

        List<com.jomap.backend.DTOs.Notifications.NotificationResponse> result = notifications.stream()
                .map(n -> {
                    String fromUsername = null;
                    String fromUserProfileImage = null;
                    if (n.getFromUser() != null) {
                        try {
                            User fromUser = n.getFromUser();
                            fromUsername = fromUser.getUsername();
                            if (fromUser.getProfile() != null && fromUser.getProfile().getFirstName() != null) {
                                fromUsername = fromUser.getProfile().getFirstName() + " " + fromUser.getProfile().getLastName();
                            }
                            if (fromUser.getProfileImageUrl() != null) {
                                fromUserProfileImage = fromUser.getProfileImageUrl();
                            } else if (fromUser.getProfile() != null && fromUser.getProfile().getProfileImageUrl() != null) {
                                fromUserProfileImage = fromUser.getProfile().getProfileImageUrl();
                            }
                        } catch (Exception e) {
                            System.out.println("Error mapping profile info for notification: " + e.getMessage());
                        }
                    }
                    return com.jomap.backend.DTOs.Notifications.NotificationResponse.builder()
                            .id(n.getId())
                            .text(n.getText())
                            .type(n.getType().name())
                            .category(n.getCategory().name())
                            .toUserId(n.getToUser().getId())
                            .fromUserId(n.getFromUser() != null ? n.getFromUser().getId() : null)
                            .fromUsername(fromUsername)
                            .fromUserProfileImage(fromUserProfileImage)
                            .activityId(n.getActivityId())
                            .postId(n.getPostId())
                            .offerId(n.getOfferId())
                            .locationId(n.getLocationId())
                            .isRead(n.getIsRead())
                            .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.success("تم تحميل قائمة الإشعارات بنجاح", result);
    }
}
package com.jomap.backend.Services.Activities;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;

import lombok.RequiredArgsConstructor;

import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final PostRepository postRepository;
    private final LocationRepo locationRepo;
    private final com.jomap.backend.Entities.Activities.RegistrationRepository registrationRepository;
    private final com.jomap.backend.Services.Notefications.EmailService emailService;

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
        activity.setActivityLocation(request.getActivityLocation());
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
                Post.PostType.COMMUNITY
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
    public ApiResponse<List<ActivityResponse>> getApprovedActivities() {
        List<ActivityResponse> activities = activityRepository.findByStatus(ActivityStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Approved Activities fetched successfully", activities);
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
        if (!activity.getGovernorate().getId().equals(request.getGovernorateId())) changes.add("المحافظة");

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setActivityLocation(request.getActivityLocation());
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(time, formatter);
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
        if (activity.getCreatedBy() != null) {
            Optional<LocationList> locationOpt = locationRepo.findByOwnerId(activity.getCreatedBy().getId());
            if (locationOpt.isPresent()) {
                locationPhone = locationOpt.get().getPhoneNumber();
                locationEmail = locationOpt.get().getEmail();
            } else {
                locationPhone = activity.getCreatedBy().getPhoneNumber();
                locationEmail = activity.getCreatedBy().getEmail();
            }
        }

        int actualAttendeesCount = registrationRepository.countByActivityIdAndStatus(activity.getId(), com.jomap.backend.Entities.Activities.RegistrationStatus.APPROVED);

        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .activityLocation(activity.getActivityLocation())
                .governorateId(activity.getGovernorate().getId())
                .governorateName(activity.getGovernorate().getName())
                .imageUrl(activity.getImageUrl())
                .latitude(activity.getLatitude())
                .price(activity.getPrice())
                .oldPrice(activity.getOldPrice())
                .maxCapacity(activity.getMaxCapacity())
                .attendeesCount(actualAttendeesCount)
                .longitude(activity.getLongitude())
                .statusId((long) activity.getStatus().getId())
                .createdById(activity.getCreatedBy().getId())
                .createdByUsername(activity.getCreatedBy().getUsername())
                .scheduleType(activity.getScheduleType())
                .totalActualDays(activity.getTotalActualDays())
                .schedules(scheduleDtos)
                .locationPhone(locationPhone)
                .locationEmail(locationEmail)
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
    public ApiResponse<com.jomap.backend.DTOs.Activities.RegistrationResponse> updateRegistrationStatus(Long registrationId, String statusStr) {
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

        registration.setStatus(status);
        com.jomap.backend.Entities.Activities.Registration savedRegistration = registrationRepository.save(registration);

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
        User user = userOptional.get();

        Optional<com.jomap.backend.Entities.Activities.Registration> regOpt = registrationRepository.findByActivityIdAndUserId(activityId, user.getId());
        if (regOpt.isEmpty()) {
            return ApiResponse.success("لا يوجد تسجيل", null);
        }

        return ApiResponse.success("تم جلب التسجيل بنجاح", mapToRegistrationResponse(regOpt.get()));
    }
}
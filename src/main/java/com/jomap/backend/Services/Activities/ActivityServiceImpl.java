package com.jomap.backend.Services.Activities;

import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Activities.CreateActivityRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional // لضمان استقرار حفظ البيانات في نيون (PostgreSQL)
    public ApiResponse<ActivityResponse> createActivity(CreateActivityRequest request) {

        // 1. جلب المستخدم الحالي من التوكن لضمان الأمان
        User author = getCurrentUser();
        if (author == null) {
            return ApiResponse.error("يجب تسجيل الدخول أولاً");
        }

        // 2. تشييك منطق التاريخ: التأكد أن التاريخ ليس في الماضي
        try {
            LocalDate activityDate = LocalDate.parse(request.getDate());
            if (activityDate.isBefore(LocalDate.now())) {
                return ApiResponse.error("لا يمكن إنشاء فعالية بتاريخ قديم، يرجى اختيار تاريخ اليوم أو تاريخ مستقبلي");
            }
        } catch (Exception e) {
            return ApiResponse.error("صيغة التاريخ غير صحيحة");
        }

        // 3. تشييك منطق الإحداثيات: إذا وُجد واحد، يجب وجود الآخر
        if ((request.getLatitude() != null && request.getLongitude() == null) ||
                (request.getLatitude() == null && request.getLongitude() != null)) {
            return ApiResponse.error("يجب إرسال كل من خطوط الطول والعرض معاً أو تركهما فارغين");
        }

        // 4. بناء الـ Entity (البيانات هنا مضمونة الصحة بفضل الـ @Valid في الـ
        // Controller)
        Activity activity = new Activity();
        activity.setTitle(request.getTitle().trim());
        activity.setDescription(request.getDescription().trim());
        activity.setDate(request.getDate());
        activity.setTime(request.getTime());
        activity.setLocationName(request.getLocationName().trim());

        // التعامل مع السعر
        String price = (request.getPrice() == null || request.getPrice().isBlank()) ? "0" : request.getPrice();
        activity.setPrice(price);

        // الحقول الاختيارية (الإحداثيات والصورة)
        activity.setImageUrl(request.getImageUrl());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());

        // 5. عملية الربط (بناءً على التوكن والمحافظة المختارة)
        activity.setUserId(author.getId());
        activity.setGovernorateId(request.getGovernorateId());

        // 6. الحفظ في قاعدة بيانات نيون
        Activity savedActivity = activityRepository.save(activity);

        // 7. إرجاع النتيجة بتنسيق ApiResponse المعتمد في المشروع
        return ApiResponse.success("تم إنشاء الفعالية بنجاح", toResponse(savedActivity));
    }

    // ميثود لجلب بيانات المستخدم صاحب التوكن الحالي
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        // البحث عن اليوزر باستخدام الإيميل المخزن في التوكن
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    // تحويل الـ Entity إلى DTO لإرساله للأندرويد
    private ActivityResponse toResponse(Activity s) {
        ActivityResponse r = new ActivityResponse();
        r.setActivityId(s.getId());
        r.setTitle(s.getTitle());
        r.setDescription(s.getDescription());
        r.setDate(s.getDate());
        r.setTime(s.getTime());
        r.setPrice(s.getPrice());
        r.setImageUrl(s.getImageUrl());
        r.setLocationName(s.getLocationName());
        r.setLatitude(s.getLatitude());
        r.setLongitude(s.getLongitude());
        r.setAttendeesCount(s.getViewsCount());
        r.setGovernorateId(s.getGovernorateId());
        r.setStatus(s.getStatus());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
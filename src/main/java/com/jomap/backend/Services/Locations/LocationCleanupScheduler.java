package com.jomap.backend.Services.Locations;

import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Offers.Offer;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Feedback.Feedback;
import com.jomap.backend.Entities.Feedback.FeedbackRepository;
import com.jomap.backend.Entities.Feedback.TargetType;

@Component
@AllArgsConstructor
@Slf4j
public class LocationCleanupScheduler {

    private final LocationRepo locationRepository;
    private final ActivityRepository activityRepository;
    private final OfferRepo offerRepo;
    private final PostRepository postRepository;
    private final FeedbackRepository feedbackRepository;

    /**
     * ⏳ وظيفة مجدولة بالخلفية (Cron Job) تنطلق تلقائياً رأس كل ساعة فلكية.
     * تقوم بفحص المنشآت المجدولة للحذف والتي مر على طلب حذفها 24 ساعة أو أكثر،
     * ليتم تصفيتها عبر إخفاء البيانات (Soft Cascade Delete).
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredLocations() {
        log.info("🎯 [Scheduler] بدء الفحص الدوري للمنشآت المجدولة للحذف (Soft Delete)...");
        
        // حساب نقطة المهلة الزمنية الحرج (الوقت الحالي ناقص 24 ساعة)
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(24);
        
        // جلب قائمة المنشآت التي تنطبق عليها شروط الحذف النهائي من الـ Repo
        List<LocationList> expiredLocations = locationRepository.findByStatusAndDeletedAtBefore(
                LocationStatus.DELETED, 
                thresholdTime
        );

        if (!expiredLocations.isEmpty()) {
            log.warn("⚠️ [Scheduler] تم العثور على ({}) منشآت انتهت مهلتها. جاري الحذف الوهمي المتسلسل...", expiredLocations.size());
            
            for (LocationList location : expiredLocations) {
                // 1. Soft delete location
                location.setIsDeleted(true);
                locationRepository.save(location);

                // 2. Soft delete activities
                List<Activity> activities = activityRepository.findByLocationId(location.getId());
                for (Activity activity : activities) {
                    activity.setIsDeleted(true);
                    activityRepository.save(activity);
                }

                // 3. Soft delete offers
                List<Offer> offers = offerRepo.findByLocationId(location.getId());
                for (Offer offer : offers) {
                    offer.setIsDeleted(true);
                    offerRepo.save(offer);
                }

                // 4. Soft delete posts (associated with owner and category 'OWNER')
                if (location.getOwner() != null) {
                    List<Post> posts = postRepository.findByAuthorId(location.getOwner().getId());
                    for (Post post : posts) {
                        if ("OWNER".equalsIgnoreCase(post.getCategory())) {
                            post.setIsDeleted(true);
                            postRepository.save(post);
                        }
                    }
                }

                // 5. Soft delete feedbacks
                List<Feedback> feedbacks = feedbackRepository.findByTargetTypeAndTargetId(TargetType.LOCATION, location.getId());
                for (Feedback feedback : feedbacks) {
                    feedback.setIsDeleted(true);
                    feedbackRepository.save(feedback);
                }
            }
            
            log.info("✅ [Scheduler] تم مسح المنشآت المنتهية وتصفية النظام (Soft Delete) بالكامل بنجاح.");
        } else {
            log.info("ℹ️ [Scheduler] الفحص الافتراضي انتهى: لا توجد أي منشآت منتهية المهلة حالياً.");
        }
    }
}
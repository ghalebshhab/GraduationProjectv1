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

@Component
@AllArgsConstructor
@Slf4j
public class LocationCleanupScheduler {

    private final LocationRepo locationRepository;

    /**
     * ⏳ وظيفة مجدولة بالخلفية (Cron Job) تنطلق تلقائياً رأس كل ساعة فلكية.
     * تقوم بفحص المنشآت المجدولة للحذف والتي مر على طلب حذفها 24 ساعة أو أكثر،
     * ليتم تصفيتها ومسحها نهائياً من قاعدة البيانات (Hard Delete).
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredLocations() {
        log.info("🎯 [Scheduler] بدء الفحص الدوري للمنشآت المجدولة للحذف النهائي...");
        
        // حساب نقطة المهلة الزمنية الحرج (الوقت الحالي ناقص 24 ساعة)
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(24);
        
        // جلب قائمة المنشآت التي تنطبق عليها شروط الحذف النهائي من الـ Repo
        List<LocationList> expiredLocations = locationRepository.findByStatusAndDeletedAtBefore(
                LocationStatus.DELETED, 
                thresholdTime
        );

        if (!expiredLocations.isEmpty()) {
            log.warn("⚠️ [Scheduler] تم العثور على ({}) منشآت انتهت مهلة الـ 24 ساعة الخاصة بها. جاري المسح القطعي...", expiredLocations.size());
            
            // تنفيذ المسح النهائي الفعلي من جدول الداتابيز
            locationRepository.deleteAll(expiredLocations);
            
            log.info("✅ [Scheduler] تم مسح المنشآت المنتهية وتصفية النظام بالكامل بنجاح.");
        } else {
            log.info("ℹ️ [Scheduler] الفحص الافتراضي انتهى: لا توجد أي منشآت منتهية المهلة حالياً.");
        }
    }
}
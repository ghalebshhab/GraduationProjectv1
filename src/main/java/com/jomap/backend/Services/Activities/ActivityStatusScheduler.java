package com.jomap.backend.Services.Activities;

import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivitySchedule;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityStatusScheduler {

    private final ActivityRepository activityRepository;

    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
    @org.springframework.transaction.annotation.Transactional
    public void updateCompletedActivities() {
        log.info("Running scheduled job to check for completed activities...");
        LocalDateTime now = LocalDateTime.now();

        List<Activity> activeActivities = activityRepository.findByStatus(ActivityStatus.APPROVED);
        List<Activity> postponedActivities = activityRepository.findByStatus(ActivityStatus.POSTPONED);
        
        checkAndUpdate(activeActivities, now);
        checkAndUpdate(postponedActivities, now);
    }

    private void checkAndUpdate(List<Activity> activities, LocalDateTime now) {
        for (Activity activity : activities) {
            List<ActivitySchedule> schedules = activity.getSchedules();
            if (schedules == null || schedules.isEmpty()) {
                continue;
            }

            LocalDateTime latestEndDateTime = null;
            for (ActivitySchedule schedule : schedules) {
                LocalDateTime endDateTime = getEndDateTime(schedule);
                if (endDateTime != null) {
                    if (latestEndDateTime == null || endDateTime.isAfter(latestEndDateTime)) {
                        latestEndDateTime = endDateTime;
                    }
                }
            }

            if (latestEndDateTime != null && latestEndDateTime.isBefore(now)) {
                log.info("Activity ID {} has completed. Updating status to COMPLETED.", activity.getId());
                activity.setStatus(ActivityStatus.COMPLETED);
                activityRepository.save(activity);
            }
        }
    }

    private LocalDateTime getEndDateTime(ActivitySchedule schedule) {
        try {
            LocalDate date = parseDate(schedule.getDate());
            LocalTime time = parseTime(schedule.getEndTime());
            if (date != null && time != null) {
                return LocalDateTime.of(date, time);
            }
        } catch (Exception e) {
            log.warn("Failed to parse date/time for schedule ID {}", schedule.getId());
        }
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                } catch (Exception exc) {
                    return null;
                }
            }
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) return null;
        try {
            return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH));
        } catch (Exception e) {
            try {
                return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH));
            } catch (Exception e2) {
                try {
                    return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception ex) {
                    return null;
                }
            }
        }
    }
}

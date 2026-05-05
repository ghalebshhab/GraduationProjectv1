package com.jomap.backend.Entities.Activities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    // رح نحتاج هاي الميثود عشان نجيب فعاليات محافظة معينة للأندرويد
    List<Activity> findByGovernorateId(Long governorateId);

    // ورح نحتاج هاي عشان الداشبورد يعرض فعاليات يوزر (منظم) معين
    List<Activity> findByUserId(Long userId);
}
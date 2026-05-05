package com.jomap.backend.DTOs.Activities;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long activityId;              // ID الفعالية عشان نفتح صفحة التفاصيل
    private String title;         // اسم الفعالية (مثل: IT DAY 2)
    private String description;   // نبذة عن الفعالية
    private String date;          // التاريخ (2 ديسمبر 2025)
    private String time;          // الوقت
    private String price;         // السعر (10 JOD)
    private String imageUrl;      // رابط البوستر اللي رح يظهر بالـ Community
    private String locationName;  // الموقع (الجامعة الهاشمية)
    
    // بيانات إضافية للخريطة والفلترة
    private Double latitude;
    private Double longitude;
    private int attendeesCount;   // عدد المهتمين (537 people attending)

    // روابط الربط
    private Long governorateId;   // ID المحافظة عشان تظهر بالصفحة الصح

    // بيانات الداشبورد والحالة
    private String status;
    private LocalDateTime createdAt;
}
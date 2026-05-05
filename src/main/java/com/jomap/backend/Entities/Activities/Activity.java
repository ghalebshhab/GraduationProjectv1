package com.jomap.backend.Entities.Activities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private String date; // تاريخ الفعالية (مثل: 2026-05-15)
    private String time; // وقت الفعالية (مثل: 10:00 AM)
    private String price;
    private String imageUrl;
    private String locationName; // اسم المكان اللي بتصير فيه الفعالية

    private Double latitude;  // نوع Double عشان يقبل أرقام عشرية دقيقة
    private Double longitude;

    // روابط الربط الأساسية (Foreign Keys بالمنطق)
    private Long userId;      // رقم اليوزر اللي عمل الفعالية
    private Long governorateId; // رقم المحافظة (1-12) عشان صفحة المحافظات والداشبورد

    // حالة الفعالية (مهم جداً للداشبورد)
    private String status = "ACTIVE"; // القيم: ACTIVE, DELETED, FLAGGED

    // تاريخ الإنشاء (عشان إحصائيات النمو في الداشبورد)
    private LocalDateTime createdAt = LocalDateTime.now();

    // عداد للمشاهدات (إحصائية إضافية للداشبورد)
    private int viewsCount = 0;
}
package com.jomap.backend.DTOs.Activities;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class UpdateActivityRequest {

    @NotBlank(message = "عنوان النشاط او الفعالية مطلوب")
    @Size(min = 5, max = 100, message = "العنوان يجب أن يكون بين 5 و 100 حرف")
    private String title;

    @NotBlank(message = "وصف النشاط او الفعالية مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description;

    @NotBlank(message = "نوع الجدولة مطلوب")
    private String scheduleType;

    @NotNull(message = "إجمالي عدد الأيام الفعلية مطلوب")
    private Integer totalActualDays;

    @NotNull(message = "قائمة المواعيد والأوقات مطلوبة")
    private List<ActivitySchedule> schedules;

    @NotBlank(message = "مكان النشاط او الفعالية مطلوب")
    private String activityLocation;

    @NotNull(message = "معرف المحافظة مطلوب")
    private Long governorateId;

    private Long locationId;

    @Min(value = 0, message = "السعر لا يمكن أن يكون أقل من صفر")
    private Double price;

    @Min(value = 1, message = "سعة المقاعد يجب أن تكون على الأقل 1")
    private Integer maxCapacity;

    private String imageUrl;
    private Double latitude;
    private Double longitude;
}
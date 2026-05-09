package com.jomap.backend.DTOs.Activities;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateActivityRequest {

    @NotBlank(message = "عنوان النشاط مطلوب")
    @Size(min = 5, max = 100, message = "العنوان يجب أن يكون بين 5 و 100 حرف")
    private String title;

    @NotBlank(message = "وصف النشاط مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description;

    @NotBlank(message = "التاريخ مطلوب")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "التاريخ يجب أن يكون بصيغة YYYY-MM-DD")
    private String date;

    @NotBlank(message = "الوقت مطلوب")
    @Pattern(regexp = "^(0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$", message = "الوقت يجب أن يكون بصيغة 10:00 AM")
    private String time;

    @NotBlank(message = "مكان النشاط مطلوب")
    private String activityLocation;

    @NotNull(message = "معرف المحافظة مطلوب")
    private Long governorateId;

    @Min(value = 0, message = "السعر لا يمكن أن يكون أقل من صفر")
    private Double price = 0.0;

    @Min(value = 0, message = "عدد الحضور لا يمكن أن يكون أقل من صفر")
    private Integer attendeesCount = 0;

    private String imageUrl;

    private Double latitude;

    private Double longitude;
}
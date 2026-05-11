package com.jomap.backend.DTOs.Activities;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateActivityRequest {

    @NotBlank(message = "عنوان النشاط مطلوب")
    @Size(min = 5, max = 100, message = "العنوان يجب أن يكون بين 5 و 100 حرف")
    private String title;

    @NotBlank(message = "وصف النشاط مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description;

    private String date;

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
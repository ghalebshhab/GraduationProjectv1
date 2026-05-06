package com.jomap.backend.DTOs.Events;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateEventRequest {

    @NotBlank(message = "عنوان الفعالية مطلوب")
    @Size(min = 5, max = 100, message = "العنوان يجب أن يكون بين 5 و 100 حرف")
    private String title;

    @NotBlank(message = "الوصف مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description;

    @NotBlank(message = "التاريخ مطلوب")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "التاريخ يجب أن يكون بصيغة YYYY-MM-DD")
    private String date;

    @NotBlank(message = "الوقت مطلوب")
    @Pattern(regexp = "^(0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$", message = "الوقت يجب أن يكون بصيغة 10:00 AM")
    private String time;

    @NotBlank(message = "اسم الموقع مطلوب")
    private String locationName;

    @NotBlank(message = "المحافظة مطلوبة")
    private String governorate;

    private String imageUrl;

    private Double latitude;

    private Double longitude;
}
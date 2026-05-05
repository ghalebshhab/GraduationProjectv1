package com.jomap.backend.DTOs.Activities;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {

    @NotBlank(message = "عنوان الفعالية مطلوب")
    @Size(min = 5, max = 100, message = "العنوان يجب أن يكون بين 5 و 100 حرف")
    private String title; // إجباري

    @NotBlank(message = "الوصف مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description; // إجباري

    @NotBlank(message = "التاريخ مطلوب")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "التاريخ يجب أن يكون بصيغة YYYY-MM-DD")
    private String date; // إجباري

    @NotBlank(message = "الوقت مطلوب")
    @Pattern(regexp = "^(0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$", message = "الوقت يجب أن يكون بصيغة 10:00 AM")
    private String time; // إجباري

    @NotBlank(message = "المكان مطلوب")
    private String locationName; // إجباري

    private String price; // اختياري (ممكن نكتب "مجاني")

    @URL(message = "يجب أن يكون رابط الصورة صحيحاً")
    private String imageUrl; // اختياري

    // إحداثيات اختيارية (ممكن تكون null)
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    private Integer attendeesCount; // اختياري (Default: 0)

    // IDs إجبارية للربط
    @NotNull(message = "المحافظة مطلوبة")
    @Min(value = 1, message = "رقم المحافظة يجب أن يكون بين 1 و 12")
    @Max(value = 12, message = "رقم المحافظة يجب أن يكون بين 1 و 12")
    private Long governorateId;
}
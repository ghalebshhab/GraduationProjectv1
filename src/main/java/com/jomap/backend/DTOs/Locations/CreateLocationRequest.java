package com.jomap.backend.DTOs.Locations;

import com.jomap.backend.Entities.Locations.LocationCategory;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLocationRequest {

    @NotBlank(message = "الشعار مطلوب") 
    private String logoUrl; 

    @NotBlank(message = "اسم الموقع او الفريق مطلوب")
    @Size(min = 3, max = 50, message = "الاسم يجب أن يكون بين 3 و 50 حرف")
    private String name;

    @NotBlank(message = "وصف الموقع او الفريق مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 احرف على الأقل")
    private String description;

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "يرجى إدخال بريد إلكتروني صحيح")
     private String email;     
     
     @NotBlank(message = "رقم الهاتف مطلوب")
    private String phoneNumber; 

    @NotNull(message = "إحداثيات الموقع او الفريق مطلوبة")
    private Double latitude;

    @NotNull(message = "إحداثيات الموقع او الفريق مطلوبة")
    private Double longitude;
    
    @NotNull(message = "معرف المحافظة مطلوب")
    private Long governorateId; 

    @NotNull(message = "تصنيف الموقع مطلوب")
    private LocationCategory category;

    private String facebookUrl;
    private String instagramUrl;
    private String linkedInUrl;
    private String workingHours;

    private String ownerUpdate; 
}
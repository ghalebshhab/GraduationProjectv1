package com.jomap.backend.DTOs.Offers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferRequest {

    @NotBlank(message = "بوستر العرض أو الخصم مطلوب")
    private String imageUrl;

    @NotBlank(message = "عنوان العرض أو الخصم مطلوب")
    @Size(min = 5, max = 100, message = "عنوان العرض يجب أن يكون بين 5 و 100 حرف")
    private String title;

    @NotBlank(message = "تفاصيل ووصف العرض مطلوب")
    @Size(min = 10, max = 2000, message = "الوصف يجب أن يكون 10 أحرف على الأقل")
    private String description;

    @NotBlank(message = "نوع جدولة العرض مطلوب (يوم واحد؟ عدة أيام؟)")
    private String scheduleType;

    @NotBlank(message = "تاريخ بداية العرض مطلوب")
    private String startDate;

    @NotBlank(message = "تاريخ نهاية العرض مطلوب")
    private String endDate;

    private String startTime = "12:00 AM"; // قيمة افتراضية ليوم كامل
    private String endTime = "11:59 PM";   // قيمة افتراضية ليوم كامل

    @NotNull(message = "معرف المنشأة مطلوب")
    private Long locationId;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "معرف المحافظة مطلوب")
    private Long governorateId;

    @NotNull(message = "قائمة المنتجات المشمولة بالعرض مطلوبة")
    @Size(min = 1, message = "يجب إضافة منتج واحد على الأقل للعرض")
    private List<OfferProductRequest> products;

    private Integer clicksCount = 0;
    
    private Long renewedFromOfferId;
}
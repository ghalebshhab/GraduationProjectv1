package com.jomap.backend.DTOs.Offers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferProductRequest {

    @NotBlank(message = "اسم المنتج أو الخدمة مطلوب")
    private String productName;

    @NotNull(message = "السعر قبل الخصم مطلوب")
    @Min(value = 0, message = "السعر قبل الخصم لا يمكن أن يكون أقل من صفر")
    private Double priceBefore;

    @NotNull(message = "السعر بعد الخصم مطلوب")
    @Min(value = 0, message = "السعر بعد الخصم لا يمكن أن يكون أقل من صفر")
    private Double priceAfter;
}
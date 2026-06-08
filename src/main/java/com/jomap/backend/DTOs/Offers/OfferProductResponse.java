package com.jomap.backend.DTOs.Offers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferProductResponse {
    private Long id;
    private String productName;
    private Double priceBefore;
    private Double priceAfter;
}

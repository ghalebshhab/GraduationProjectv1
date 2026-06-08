package com.jomap.backend.Services.Offers;

import com.jomap.backend.DTOs.Offers.OfferRequest;
import com.jomap.backend.DTOs.Offers.OfferResponse;
import com.jomap.backend.DTOs.ApiResponse;

public interface OfferService {
    ApiResponse<OfferResponse> createOffer(OfferRequest request, String email);
}
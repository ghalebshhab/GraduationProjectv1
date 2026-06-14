package com.jomap.backend.Services.Offers;

import com.jomap.backend.DTOs.Offers.OfferRequest;
import com.jomap.backend.DTOs.Offers.OfferResponse;
import com.jomap.backend.DTOs.ApiResponse;
import java.util.List;

public interface OfferService {
    ApiResponse<OfferResponse> createOffer(OfferRequest request, String email);
    ApiResponse<List<OfferResponse>> getMyOffers(String email);
    ApiResponse<OfferResponse> getOfferById(Long offerId);
    ApiResponse<List<OfferResponse>> getOffersByLocation(Long locationId);
    ApiResponse<OfferResponse> cancelOffer(Long offerId, String email);
    ApiResponse<OfferResponse> deleteOffer(Long offerId, String email);
    ApiResponse<String> toggleFavoriteOffer(Long offerId, String userEmail);
    ApiResponse<List<OfferResponse>> getFavoriteOffers(String userEmail);
    ApiResponse<List<OfferResponse>> getAllOffers();
}
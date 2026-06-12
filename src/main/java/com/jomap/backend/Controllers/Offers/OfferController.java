package com.jomap.backend.Controllers.Offers;

import com.jomap.backend.DTOs.Offers.OfferRequest;
import com.jomap.backend.DTOs.Offers.OfferResponse;
import com.jomap.backend.Services.Offers.OfferService;
import com.jomap.backend.DTOs.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/offers") // تعديل المسار ليكون عام ونظيف ومطابق للأنشطة
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping("/create") // مسار صريح ونظيف لإنشاء العرض
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
            @Valid @RequestBody OfferRequest request, 
            Principal principal) { // سحب المستخدم الموثق حالياً من الـ Token بالخلفية

        try {
            // تمرير الـ request وإيميل المستخدم دغري للـ Service
            ApiResponse<OfferResponse> response = offerService.createOffer(request, principal.getName());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("حدث خطأ داخلي في الخادم: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<java.util.List<OfferResponse>>> getMyOffers(Principal principal) {
        try {
            ApiResponse<java.util.List<OfferResponse>> response = offerService.getMyOffers(principal.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("حدث خطأ أثناء جلب العروض: " + e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<java.util.List<OfferResponse>>> getOffersByLocation(@PathVariable Long locationId) {
        ApiResponse<java.util.List<OfferResponse>> response = offerService.getOffersByLocation(locationId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferById(@PathVariable Long id) {
        ApiResponse<OfferResponse> response = offerService.getOfferById(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OfferResponse>> cancelOffer(@PathVariable Long id, Principal principal) {
        try {
            ApiResponse<OfferResponse> response = offerService.cancelOffer(id, principal.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("حدث خطأ أثناء إلغاء العرض: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferResponse>> deleteOffer(@PathVariable Long id, Principal principal) {
        try {
            ApiResponse<OfferResponse> response = offerService.deleteOffer(id, principal.getName());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("حدث خطأ أثناء حذف العرض: " + e.getMessage()));
        }
    }
}
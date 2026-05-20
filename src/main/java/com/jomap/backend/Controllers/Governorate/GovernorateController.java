package com.jomap.backend.Controllers.Governorate;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Governorate.GovernorateDetailsResponse;
import com.jomap.backend.DTOs.Governorate.ImageRequest;
import com.jomap.backend.DTOs.Governorate.PlaceRequest;
import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Governorate.Place;
import com.jomap.backend.Services.Governorate.GovernorateService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/governorates")
@RequiredArgsConstructor
public class GovernorateController {

    private final GovernorateService governorateService;

    @GetMapping
    public ResponseEntity<List<Governorate>> getAll() {
        return ResponseEntity.ok(governorateService.getAllGovernorates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GovernorateDetailsResponse>> getGovernorateDetails(@PathVariable Long id) {
        ApiResponse<GovernorateDetailsResponse> response = governorateService.getGovernorateDetails(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<?>> addImageUrl(
            @PathVariable Long id,
            @RequestBody ImageRequest request) {

        ApiResponse<?> response = governorateService.addImageToGovernorate(id, request.getImageUrl());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/places")
    public ResponseEntity<ApiResponse<?>> addPlace(
            @PathVariable Long id,
            @RequestBody PlaceRequest request) {

        // سيمرر الإضافة بنجاح وتلقائياً لجدول الـ places الثابت
        ApiResponse<Place> response = governorateService.addPlaceToGovernorate(
                id,
                request.getName(),
                request.getDescription(),
                request.getImageUrl()
        );
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
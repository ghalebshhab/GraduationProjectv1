package com.jomap.backend.Controllers.Gov;


import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Gove.ImageRequestDto;
import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Gove.GovernorateImage;
import com.jomap.backend.Entities.Gove.Place;
import com.jomap.backend.DTOs.Gove.PlaceRequestDto;
import com.jomap.backend.Services.Gove.GovernorateService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Governorate> getById(@PathVariable Long id) {
        return governorateService.getGovernorateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<?>> addImageUrl(
            @PathVariable Long id,
            @RequestBody ImageRequestDto request) {

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
            @RequestBody PlaceRequestDto request) {

        ApiResponse<?> response = governorateService.addPlaceToGovernorate(
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
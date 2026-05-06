package com.jomap.backend.Controllers.Places;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Places.CreatePlaceRequest;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.DTOs.Places.UpdatePlaceRequest;
import com.jomap.backend.Entities.Places.PlaceCategory;
import com.jomap.backend.Services.Places.PlaceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@AllArgsConstructor
public class PlaceController {

    private final PlaceService placeService;



    @PostMapping
    public ResponseEntity<ApiResponse<PlaceResponse>> createPlace(
            @RequestBody CreatePlaceRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<PlaceResponse> response =
                placeService.createPlace(request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> updatePlace(
            @PathVariable Long placeId,
            @RequestBody UpdatePlaceRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<PlaceResponse> response =
                placeService.updatePlace(placeId, request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getPlaces(
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) PlaceCategory category
    ) {
        ApiResponse<List<PlaceResponse>> response =
                placeService.getPlaces(governorate, category);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> getPlaceById(
            @PathVariable Long placeId
    ) {
        ApiResponse<PlaceResponse> response =
                placeService.getPlaceById(placeId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PlaceResponse>> getMyPlace(
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<PlaceResponse> response =
                placeService.getMyPlace(principal.getName());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> deactivatePlace(
            @PathVariable Long placeId,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<PlaceResponse> response =
                placeService.deactivatePlace(placeId, principal.getName());

        return ResponseEntity.ok(response);
    }
}
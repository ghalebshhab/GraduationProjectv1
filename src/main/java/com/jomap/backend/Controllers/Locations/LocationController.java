package com.jomap.backend.Controllers.Locations;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.Services.Locations.LocationService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@AllArgsConstructor
public class LocationController {

    private final LocationService locationService; 

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(
            @RequestBody CreateLocationRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<LocationResponse> response =
                locationService.createLocation(request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
            @PathVariable Long locationId,
            @RequestBody UpdateLocationRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<LocationResponse> response =
                locationService.updateLocation(locationId, request, principal.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocations(
            @RequestParam(required = false) Long governorateId, // تعديل لـ Long governorateId
            @RequestParam(required = false) LocationCategory category
    ) {
        ApiResponse<List<LocationResponse>> response =
                locationService.getLocations(governorateId, category);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationResponse>> getLocationById(
            @PathVariable Long locationId
    ) {
        ApiResponse<LocationResponse> response =
                locationService.getLocationById(locationId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<LocationResponse>> getMyLocation(
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<LocationResponse> response =
                locationService.getMyLocation(principal.getName());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{locationId}/approve")
    public ResponseEntity<ApiResponse<LocationResponse>> approveLocation(
            @PathVariable Long locationId
    ) {
        ApiResponse<LocationResponse> response = locationService.approveLocation(locationId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationResponse>> deactivateLocation(
            @PathVariable Long locationId,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }

        ApiResponse<LocationResponse> response =
                locationService.deactivateLocation(locationId, principal.getName());

        return ResponseEntity.ok(response);
    }

// تحديث صورة الغلاف
@PatchMapping("/{id}/update-cover")
public ResponseEntity<?> updateCover(@PathVariable Long id, @RequestBody UpdateLocationRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }
    locationService.updateCover(id, request, principal.getName());
    return ResponseEntity.ok("تم تحديث غلاف الموقع بنجاح");
}

// تحديث اللوجو
@PatchMapping("/{id}/update-logo")
public ResponseEntity<?> updateLogo(@PathVariable Long id, @RequestBody UpdateLocationRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("User is not authenticated"));
        }
    locationService.updateLogo(id, request, principal.getName() );
    return ResponseEntity.ok("تم تحديث لوجو الموقع بنجاح");
}

}
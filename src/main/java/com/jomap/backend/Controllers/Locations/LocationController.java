package com.jomap.backend.Controllers.Locations;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.Services.Community.Posts.PostsServices;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Services.Locations.LocationService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@AllArgsConstructor
public class LocationController {

    private final LocationService locationService; 
    private final PostsServices postsServices;
    private final LocationRepo locationRepo;

    @GetMapping("/{locationId}/posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getLocationPosts(
            @PathVariable("locationId") Long locationId
    ) {
        LocationList location = locationRepo.findById(locationId).orElse(null);
        if (location == null || location.getOwner() == null) {
            return ResponseEntity.ok(ApiResponse.error("Location not found"));
        }
        Long ownerId = location.getOwner().getId();
        return ResponseEntity.ok(postsServices.getAllPosts(ownerId.intValue(), "OWNER"));
    } 

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.toggleFavoriteLocation(id, principal.getName()));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getFavorites(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.getFavoriteLocations(principal.getName()));
    }

    // 1. إنشاء موقع جديد
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.createLocation(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 2. تحديث البيانات الشاملة والأساسية للموقع (مطابق للفرونت مية بالمية)
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateLocationRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.updateLocation(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 3. الدالة الموحدة الذكية للتحكم بحالة الموقع الشاملة (تنشيط PUBLISHED / تعطيل DEACTIVATED / حذف DELETED)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LocationResponse>> changeLocationStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.changeLocationStatus(id, status, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 4. جلب كل المواقع النشطة والمعتمدة على الخريطة للزوار
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocations(
            @RequestParam(required = false) Long governorateId, 
            @RequestParam(required = false) LocationCategory category
    ) {
        ApiResponse<List<LocationResponse>> response = locationService.getLocations(governorateId, category);
        return ResponseEntity.ok(response);
    }

    // 5. جلب تفاصيل موقع محدد بالـ ID لفرش الحقول بالفرونت
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> getLocationById(
            @PathVariable("id") Long id
    ) {
        ApiResponse<LocationResponse> response = locationService.getLocationById(id);
        return ResponseEntity.ok(response);
    }

    // 6. جلب الموقع الخاص بالمسؤول الحالي دغري للـ Dashboard
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<LocationResponse>> getMyLocation(
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.getMyLocation(principal.getName());
        return ResponseEntity.ok(response);
    }

    // 7. اعتماد وقبول الموقع من الـ Admin لنشره
    @PatchMapping("/{locationId}/approve")
    public ResponseEntity<ApiResponse<LocationResponse>> approveLocation(
            @PathVariable Long locationId
    ) {
        ApiResponse<LocationResponse> response = locationService.approveLocation(locationId);
        return ResponseEntity.ok(response);
    }

    // 8. تحديث صورة الغلاف بشكل منفصل
    @PatchMapping("/{id}/update-cover")
    public ResponseEntity<ApiResponse<LocationResponse>> updateCover(
            @PathVariable Long id, 
            @RequestBody UpdateLocationRequest request, 
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.updateCover(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 9. تحديث اللوجو بشكل منفصل
    @PatchMapping("/{id}/update-logo")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLogo(
            @PathVariable Long id, 
            @RequestBody UpdateLocationRequest request, 
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }
        ApiResponse<LocationResponse> response = locationService.updateLogo(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 10. جلب متابعي منشأة معينة (لصاحب المنشأة فقط أو الادمن)
    @GetMapping("/{locationId}/followers")
    public ResponseEntity<ApiResponse<java.util.List<com.jomap.backend.DTOs.Locations.LocationFollowerResponse>>> getLocationFollowers(
            @PathVariable Long locationId,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.getLocationFollowers(locationId, principal.getName()));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<String>> followLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.followLocation(id, principal.getName()));
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<ApiResponse<String>> unfollowLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.unfollowLocation(id, principal.getName()));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<String>> blockLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.blockLocation(id, principal.getName()));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<String>> unblockLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(locationService.unblockLocation(id, principal.getName()));
    }
}
package com.jomap.backend.Controllers.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.Services.Dashboard.AdminDashboardService;
import com.jomap.backend.Services.Activities.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
@AllArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final ActivityService ActivityService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(adminDashboardService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers() {
        return ResponseEntity.ok(adminDashboardService.getUsers());
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminDashboardService.blockUser(userId));
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unblockUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminDashboardService.unblockUser(userId));
    }

    @GetMapping("/locations/pending")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getPendingLocations() {
        return ResponseEntity.ok(adminDashboardService.getPendingLocations());
    }

    @GetMapping("/locations/all")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getAllLocations() {
        return ResponseEntity.ok(adminDashboardService.getAllLocations());
    }

    @PutMapping("/locations/{locationId}/approve")
    public ResponseEntity<ApiResponse<LocationResponse>> approveLocation(
            @PathVariable Long locationId) {
        return ResponseEntity.ok(adminDashboardService.approveLocation(locationId));
    }

    @PutMapping("/locations/{locationId}/reject")
    public ResponseEntity<ApiResponse<LocationResponse>> rejectLocation(
            @PathVariable Long locationId,
            @RequestBody(required = false) java.util.Map<String, String> payload) {
        String reason = payload != null ? payload.get("rejectionReason") : null;
        return ResponseEntity.ok(adminDashboardService.rejectLocation(locationId, reason));
    }

    @PutMapping("/locations/{locationId}/deactivate")
    public ResponseEntity<ApiResponse<LocationResponse>> deactivateLocation(
            @PathVariable Long locationId) {
        return ResponseEntity.ok(adminDashboardService.deactivateLocation(locationId));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<AdminPostResponse>>> getPosts() {
        return ResponseEntity.ok(adminDashboardService.getPosts());
    }

    @PutMapping("/posts/{postId}/delete")
    public ResponseEntity<ApiResponse<AdminPostResponse>> deletePost(
            @PathVariable Long postId) {
        return ResponseEntity.ok(adminDashboardService.deletePost(postId));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<AdminReportResponse>>> getReports() {
        return ResponseEntity.ok(adminDashboardService.getReports());
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<AdminReportResponse>> resolveReport(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(adminDashboardService.resolveReport(reportId));
    }

    @GetMapping("/Activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getAllActivitiesForAdmin() {
        return ResponseEntity.ok(ActivityService.getAllActivitiesForAdmin());
    }

    @PutMapping("/Activities/{ActivityId}/approve")
    public ResponseEntity<ApiResponse<ActivityResponse>> approveActivity(
            @PathVariable Long ActivityId) {
        return ResponseEntity.ok(ActivityService.approveActivity(ActivityId));
    }

    @PutMapping("/Activities/{ActivityId}/reject")
    public ResponseEntity<ApiResponse<ActivityResponse>> rejectActivity(
            @PathVariable Long ActivityId) {
        return ResponseEntity.ok(ActivityService.rejectActivity(ActivityId));
    }
}
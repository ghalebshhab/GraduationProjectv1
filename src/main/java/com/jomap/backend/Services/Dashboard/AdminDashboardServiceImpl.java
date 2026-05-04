package com.jomap.backend.Services.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.Entities.Places.LocationList;
import com.jomap.backend.Entities.Places.LocationListRepo;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Reports.Report;
import com.jomap.backend.Entities.Reports.ReportRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final LocationListRepo placeRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;



    @Override
    public ApiResponse<AdminStatsResponse> getStats() {

        AdminStatsResponse response = new AdminStatsResponse();

        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(userRepository.countByIsActiveTrue());
        response.setBlockedUsers(userRepository.countByIsActiveFalse());

        response.setTotalPlaces(placeRepository.count());
        response.setApprovedPlaces(placeRepository.countByApprovedTrueAndActiveTrue());
        response.setPendingPlaces(placeRepository.countByApprovedFalseAndActiveTrue());
        response.setInactivePlaces(placeRepository.countByActiveFalse());

        response.setTotalPosts(postRepository.count());
        response.setActivePosts(postRepository.countByIsDeletedFalse());
        response.setDeletedPosts(postRepository.countByIsDeletedTrue());

        response.setTotalReports(reportRepository.count());
        response.setPendingReports(reportRepository.countByResolvedFalse());
        response.setResolvedReports(reportRepository.countByResolvedTrue());

        return ApiResponse.success("Admin dashboard stats fetched successfully", response);
    }

    @Override
    public ApiResponse<List<AdminUserResponse>> getUsers() {

        List<AdminUserResponse> response = userRepository.findAll()
                .stream()
                .map(this::mapUserToAdminResponse)
                .toList();

        return ApiResponse.success("Users fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminUserResponse> blockUser(Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        return ApiResponse.success("User blocked successfully", mapUserToAdminResponse(savedUser));
    }

    @Override
    public ApiResponse<AdminUserResponse> unblockUser(Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        return ApiResponse.success("User unblocked successfully", mapUserToAdminResponse(savedUser));
    }

    @Override
    public ApiResponse<List<PlaceResponse>> getPendingPlaces() {

        List<PlaceResponse> response = placeRepository.findByApprovedFalseAndActiveTrue()
                .stream()
                .map(this::mapPlaceToResponse)
                .toList();

        return ApiResponse.success("Pending places fetched successfully", response);
    }

    @Override
    public ApiResponse<List<PlaceResponse>> getAllPlaces() {

        List<PlaceResponse> response = placeRepository.findAll()
                .stream()
                .map(this::mapPlaceToResponse)
                .toList();

        return ApiResponse.success("All places fetched successfully", response);
    }

    @Override
    public ApiResponse<PlaceResponse> approvePlace(Long placeId) {

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();
        place.setApproved(true);
        place.setActive(true);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success("Place approved successfully", mapPlaceToResponse(savedPlace));
    }

    @Override
    public ApiResponse<PlaceResponse> rejectPlace(Long placeId) {

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();
        place.setApproved(false);
        place.setActive(false);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success("Place rejected successfully", mapPlaceToResponse(savedPlace));
    }

    @Override
    public ApiResponse<PlaceResponse> deactivatePlace(Long placeId) {

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();
        place.setActive(false);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success("Place deactivated successfully", mapPlaceToResponse(savedPlace));
    }

    @Override
    public ApiResponse<List<AdminPostResponse>> getPosts() {

        List<AdminPostResponse> response = postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapPostToAdminResponse)
                .toList();

        return ApiResponse.success("Posts fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminPostResponse> deletePost(Long postId) {

        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ApiResponse.error("Post not found");
        }

        Post post = postOptional.get();
        post.setIsDeleted(true);

        Post savedPost = postRepository.save(post);

        return ApiResponse.success("Post deleted successfully", mapPostToAdminResponse(savedPost));
    }

    @Override
    public ApiResponse<List<AdminReportResponse>> getReports() {

        List<AdminReportResponse> response = reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapReportToAdminResponse)
                .toList();

        return ApiResponse.success("Reports fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminReportResponse> resolveReport(Long reportId) {

        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isEmpty()) {
            return ApiResponse.error("Report not found");
        }

        Report report = reportOptional.get();
        report.setResolved(true);

        Report savedReport = reportRepository.save(report);

        return ApiResponse.success("Report resolved successfully", mapReportToAdminResponse(savedReport));
    }

    private AdminUserResponse mapUserToAdminResponse(User user) {

        AdminUserResponse response = new AdminUserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() == null ? null : user.getRole().toString());
        response.setActive(user.getIsActive());

        return response;
    }

    private PlaceResponse mapPlaceToResponse(LocationList place) {

        PlaceResponse response = new PlaceResponse();

        response.setId(place.getId());
        response.setName(place.getName());
        response.setDescription(place.getDescription());
        response.setEmail(place.getEmail());
        response.setPhoneNumber(place.getPhoneNumber());
        response.setImageUrl(place.getImageUrl());
        response.setLatitude(place.getLatitude());
        response.setLongitude(place.getLongitude());
        response.setGovernorate(place.getGovernorate());
        response.setCategory(place.getCategory());
        response.setRating(place.getRating());
        response.setReviewCount(place.getReviewCount());
        response.setActive(place.getActive());
        response.setApproved(place.getApproved());
        response.setOwnerUpdate(place.getOwnerUpdate());
        response.setCreatedAt(place.getCreatedAt());
        response.setUpdatedAt(place.getUpdatedAt());

        if (place.getOwner() != null) {
            response.setOwnerId(place.getOwner().getId());
            response.setOwnerName(place.getOwner().getUsername());
        }

        return response;
    }

    private AdminPostResponse mapPostToAdminResponse(Post post) {

        AdminPostResponse response = new AdminPostResponse();

        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setType(post.getType() == null ? null : post.getType().toString());
        response.setDeleted(post.getIsDeleted());

        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorName(post.getAuthor().getUsername());
        }

        return response;
    }

    private AdminReportResponse mapReportToAdminResponse(Report report) {

        AdminReportResponse response = new AdminReportResponse();

        response.setId(report.getId());
        response.setReason(report.getReason());
        response.setResolved(report.getResolved());

        if (report.getReportedBy() != null) {
            response.setReportedById(report.getReportedBy().getId());
            response.setReportedBy(report.getReportedBy().getUsername());
        }

        return response;
    }
}
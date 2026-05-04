package com.jomap.backend.Services.Places;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Places.CreatePlaceRequest;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.DTOs.Places.UpdatePlaceRequest;
import com.jomap.backend.Entities.Places.LocationList;
import com.jomap.backend.Entities.Places.PlaceCategory;
import com.jomap.backend.Entities.Places.LocationListRepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class PlaceServiceImpl implements PlaceService {
    private final LocationListRepo placeRepository;
    private final UserRepository userRepository;


    @Override
    public ApiResponse<PlaceResponse> createPlace(CreatePlaceRequest request, String currentUserEmail) {

        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);

        if (!userResponse.isSuccess()) {
            return ApiResponse.error(userResponse.getMessage());
        }

        User owner = userResponse.getData();

        ApiResponse<Void> validationResponse = validateCreateRequest(request);

        if (!validationResponse.isSuccess()) {
            return ApiResponse.error(validationResponse.getMessage());
        }

        if (placeRepository.existsByOwnerId(owner.getId())) {
            return ApiResponse.error("You already have a place. You can edit your existing place instead.");
        }

        LocationList place = new LocationList();

        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setEmail(request.getEmail());
        place.setPhoneNumber(request.getPhoneNumber());
        place.setImageUrl(request.getImageUrl());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setGovernorate(request.getGovernorate());
        place.setCategory(request.getCategory() == null ? PlaceCategory.OTHER : request.getCategory());
        place.setOwnerUpdate(request.getOwnerUpdate());
        place.setOwner(owner);
        place.setActive(true);
        place.setApproved(false);
        place.setRating(0.0);
        place.setReviewCount(0);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success(
                "Place created successfully. Waiting for admin approval.",
                mapToResponse(savedPlace)
        );
    }
    @Override
    public ApiResponse<PlaceResponse> updatePlace(Long placeId,
                                                  UpdatePlaceRequest request,
                                                  String currentUserEmail) {

        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);

        if (!userResponse.isSuccess()) {
            return ApiResponse.error(userResponse.getMessage());
        }

        User currentUser = userResponse.getData();

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();

        if (place.getOwner() == null || !place.getOwner().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You are not allowed to update this place");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            place.setName(request.getName());
        }

        if (request.getDescription() != null) {
            place.setDescription(request.getDescription());
        }

        if (request.getEmail() != null) {
            place.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            place.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getImageUrl() != null) {
            place.setImageUrl(request.getImageUrl());
        }

        if (request.getLatitude() != null) {
            place.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            place.setLongitude(request.getLongitude());
        }

        if (request.getGovernorate() != null && !request.getGovernorate().isBlank()) {
            place.setGovernorate(request.getGovernorate());
        }

        if (request.getCategory() != null) {
            place.setCategory(request.getCategory());
        }

        if (request.getOwnerUpdate() != null) {
            place.setOwnerUpdate(request.getOwnerUpdate());
        }

        /*
         إذا بدك بعد أي تعديل يحتاج موافقة أدمن:
         خلي هذا السطر.
         إذا ما بدك يحتاج موافقة، احذف السطر.
        */
        place.setApproved(false);

        LocationList updatedPlace = placeRepository.save(place);

        return ApiResponse.success(
                "Place updated successfully. Waiting for admin approval.",
                mapToResponse(updatedPlace)
        );
    }

    @Override
    public ApiResponse<PlaceResponse> getPlaceById(Long placeId) {

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();

        if (!Boolean.TRUE.equals(place.getActive())) {
            return ApiResponse.error("Place is not active");
        }

        return ApiResponse.success(
                "Place fetched successfully",
                mapToResponse(place)
        );
    }

    @Override
    public ApiResponse<List<PlaceResponse>> getPlaces(String governorate, PlaceCategory category) {

        List<LocationList> places;

        boolean hasGovernorate = governorate != null && !governorate.isBlank();
        boolean hasCategory = category != null;

        if (hasGovernorate && hasCategory) {
            places = placeRepository
                    .findByGovernorateIgnoreCaseAndCategoryAndActiveTrueAndApprovedTrue(governorate, category);
        } else if (hasGovernorate) {
            places = placeRepository
                    .findByGovernorateIgnoreCaseAndActiveTrueAndApprovedTrue(governorate);
        } else if (hasCategory) {
            places = placeRepository
                    .findByCategoryAndActiveTrueAndApprovedTrue(category);
        } else {
            places = placeRepository.findByActiveTrueAndApprovedTrue();
        }

        List<PlaceResponse> response = places.stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Places fetched successfully", response);
    }

    @Override
    public ApiResponse<PlaceResponse> getMyPlace(String currentUserEmail) {

        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);

        if (!userResponse.isSuccess()) {
            return ApiResponse.error(userResponse.getMessage());
        }

        User currentUser = userResponse.getData();

        Optional<LocationList> placeOptional = placeRepository.findByOwnerId(currentUser.getId());

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("You do not have a place yet");
        }

        return ApiResponse.success(
                "My place fetched successfully",
                mapToResponse(placeOptional.get())
        );
    }

    @Override
    public ApiResponse<PlaceResponse> approvePlace(Long placeId) {

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();

        place.setApproved(true);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success(
                "Place approved successfully",
                mapToResponse(savedPlace)
        );
    }

    @Override
    public ApiResponse<PlaceResponse> deactivatePlace(Long placeId, String currentUserEmail) {

        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);

        if (!userResponse.isSuccess()) {
            return ApiResponse.error(userResponse.getMessage());
        }

        User currentUser = userResponse.getData();

        Optional<LocationList> placeOptional = placeRepository.findById(placeId);

        if (placeOptional.isEmpty()) {
            return ApiResponse.error("Place not found");
        }

        LocationList place = placeOptional.get();

        if (place.getOwner() == null || !place.getOwner().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You are not allowed to delete this place");
        }

        place.setActive(false);

        LocationList savedPlace = placeRepository.save(place);

        return ApiResponse.success(
                "Place deactivated successfully",
                mapToResponse(savedPlace)
        );
    }

    private ApiResponse<Void> validateCreateRequest(CreatePlaceRequest request) {

        if (request == null) {
            return ApiResponse.error("Request body is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.error("Place name is required");
        }

        if (request.getGovernorate() == null || request.getGovernorate().isBlank()) {
            return ApiResponse.error("Governorate is required");
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            return ApiResponse.error("Location coordinates are required");
        }

        return ApiResponse.success("Valid request", null);
    }

    private ApiResponse<User> getUserByEmail(String email) {

        if (email == null || email.isBlank()) {
            return ApiResponse.error("User is not authenticated");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        return ApiResponse.success("User fetched successfully", userOptional.get());
    }

    private PlaceResponse mapToResponse(LocationList place) {

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

}

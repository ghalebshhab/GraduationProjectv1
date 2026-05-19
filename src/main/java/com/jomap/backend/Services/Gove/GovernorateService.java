package com.jomap.backend.Services.Gove;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Gove.GovernorateDetailsResponse;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import com.jomap.backend.Entities.Activities.ActivitySchedule;
import com.jomap.backend.Entities.Gove.*;
import com.jomap.backend.Entities.Locations.LocationCategory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GovernorateService {

    @Autowired
    private final GovernorateRepository governorateRepository;
    @Autowired
    private final GovernorateImageRepository imageRepository;
    @Autowired
    private final PlaceRepository placeRepository;
    @Autowired
    private ActivityRepository activityRepository;

    public List<Governorate> getAllGovernorates() {
        return governorateRepository.findAll();
    }

    public Optional<Governorate> getGovernorateById(Long id) {
        return governorateRepository.findById(id);
    }

    public ApiResponse<?> addImageToGovernorate(Long governorateId, String imageUrl) {
        Optional<Governorate> governorateOpt = governorateRepository.findById(governorateId);

        if (governorateOpt.isEmpty()) {
            return ApiResponse.error("المحافظة غير موجودة، لا يمكن إضافة الصورة");
        }

        Governorate governorate = governorateOpt.get();
        GovernorateImage image = new GovernorateImage();
        image.setImageUrl(imageUrl);
        image.setGovernorate(governorate);

        GovernorateImage savedImage = imageRepository.save(image);
        return ApiResponse.success("تمت إضافة الصورة بنجاح", savedImage);
    }

    public ApiResponse<Place> addPlaceToGovernorate(Long governorateId, String name, String description,
            String imageUrl) {
        Optional<Governorate> governorateOpt = governorateRepository.findById(governorateId);

        if (governorateOpt.isEmpty()) {
            return ApiResponse.error("المحافظة غير موجودة، لا يمكن إضافة الموقع الأثري");
        }

        Governorate governorate = governorateOpt.get();

        Place place = new Place();
        place.setName(name);
        place.setDescription(description);
        place.setImageUrl(imageUrl);
        place.setGovernorate(governorate);

        Place savedPlace = placeRepository.save(place);
        return ApiResponse.success("تم إضافة الموقع الأثري بنجاح", savedPlace);
    }

    public ApiResponse<GovernorateDetailsResponse> getGovernorateDetails(Long id) {
        Governorate gov = governorateRepository.findById(id).orElse(null);
        if (gov == null) {
            return new ApiResponse<>(false, "المحافظة غير موجودة", null);
        }

        List<String> imageUrls = gov.getImages().stream()
                .map(GovernorateImage::getImageUrl)
                .collect(Collectors.toList());

        List<Place> allPlaces = placeRepository.findByGovernorateId(id);

        List<LocationResponse> suggestions = allPlaces.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.shuffle(list);
                    return list.stream();
                }))
                .limit(5)
                .map(this::mapToPlaceResponse)
                .collect(Collectors.toList());

        List<LocationResponse> historicalPlaces = allPlaces.stream()
                .filter(p -> p.getCategory() == LocationCategory.TOURISM)
                .limit(5)
                .map(this::mapToPlaceResponse)
                .collect(Collectors.toList());

       List<ActivityResponse> approvedActivities = activityRepository
        .findByStatusAndGovernorateId(ActivityStatus.APPROVED, id).stream().limit(5)
        .<ActivityResponse>map(activity -> { 
            
            List<com.jomap.backend.DTOs.Activities.ActivitySchedule> scheduleDtos = new ArrayList<>();
            if (activity.getSchedules() != null) {
                for (ActivitySchedule schedule : activity.getSchedules()) {
                    scheduleDtos.add(com.jomap.backend.DTOs.Activities.ActivitySchedule.builder()
                            .date(schedule.getDate())
                            .dayName(schedule.getDayName())
                            .startTime(schedule.getStartTime())
                            .endTime(schedule.getEndTime())
                            .build());
                }
            }

            return ActivityResponse.builder()
                    .id(activity.getId())
                    .title(activity.getTitle())
                    .description(activity.getDescription())
                    .imageUrl(activity.getImageUrl())
                    .activityLocation(activity.getActivityLocation())
                    .governorateId(activity.getGovernorate().getId())
                    .latitude(activity.getLatitude())
                    .longitude(activity.getLongitude())
                    .price(activity.getPrice())
                    .attendeesCount(activity.getAttendeesCount())
                    .statusId((long) activity.getStatus().getId())
                    .createdById(activity.getCreatedBy().getId())
                    .createdByUsername(activity.getCreatedBy().getUsername())
                    .scheduleType(activity.getScheduleType())
                    .totalActualDays(activity.getTotalActualDays())
                    .schedules(scheduleDtos)
                    .build();
        })
        .collect(Collectors.toList());

        GovernorateDetailsResponse details = GovernorateDetailsResponse.builder()
                .id(gov.getId())
                .name(gov.getName())
                .images(imageUrls)
                .suggestions(suggestions)
                .historicalPlaces(historicalPlaces)
                .activities(approvedActivities)
                .build();

        return new ApiResponse<>(true, "تم استرجاع تفاصيل المحافظة بنجاح", details);
    }

    private LocationResponse mapToPlaceResponse(Place place) {
        if (place == null)
            return null;
        LocationResponse response = new LocationResponse();
        response.setLocationId(place.getId());
        response.setName(place.getName());
        response.setDescription(place.getDescription());
        response.setLogoUrl(place.getImageUrl());

        response.setCategory(place.getCategory() != null ? place.getCategory() : LocationCategory.OTHER);
        return response;
    }
}
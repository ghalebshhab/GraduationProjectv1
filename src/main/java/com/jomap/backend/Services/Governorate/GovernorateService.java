package com.jomap.backend.Services.Governorate;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Activities.ActivityResponse;
import com.jomap.backend.DTOs.Governorate.GovernorateDetailsResponse;
import com.jomap.backend.DTOs.Governorate.PlaceResponse;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import com.jomap.backend.Entities.Governorate.*;
import com.jomap.backend.Entities.Activities.ActivitySchedule;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Offers.OfferStatus;
import com.jomap.backend.DTOs.Offers.OfferResponse;
import com.jomap.backend.DTOs.Offers.OfferProductResponse;
import com.jomap.backend.Entities.Offers.OfferProduct;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Locations.LocationBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GovernorateService {

    private final GovernorateRepository governorateRepository;
    private final GovernorateImageRepository imageRepository;
    private final PlaceRepository placeRepository;
    private final ActivityRepository activityRepository;
    private final LocationRepo locationRepo; 
    private final OfferRepo offerRepository;
    private final UserRepository userRepository;
    private final LocationBlockRepository locationBlockRepository;

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

    public ApiResponse<Place> addPlaceToGovernorate(Long governorateId, String name, String description, String imageUrl) {
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

        // Fetch blocked location IDs for current user
        List<Long> blockedLocationIds = new ArrayList<>();
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                userRepository.findByEmail(auth.getName()).ifPresent(currentUser -> {
                    blockedLocationIds.addAll(locationBlockRepository.findBlockedLocationIdsByBlockerId(currentUser.getId()));
                });
            }
        } catch (Exception ignored) {}
        final List<Long> finalBlockedLocs = blockedLocationIds;

        // 1. سحب صور غلاف المحافظة
        List<String> imageUrls = gov.getImages().stream()
                .map(GovernorateImage::getImageUrl)
                .collect(Collectors.toList());

        // 2. سحب الأماكن الثابتة المضافة يدوياً بالمحافظة
        List<Place> staticPlaces = placeRepository.findByGovernorateId(id);

        // 3. سحب المواقع النشطة والمقبولة التي أنشأها المستخدمون في هذه المحافظة
        List<LocationList> userLocations = locationRepo.findByGovernorateIdAndActiveTrueAndApprovedTrue(id);
        if (!finalBlockedLocs.isEmpty()) {
            userLocations = userLocations.stream()
                    .filter(loc -> !finalBlockedLocs.contains(loc.getId()))
                    .collect(Collectors.toList());
        }

        // 🎯 أماكن مقترحة: User locations ONLY (excluding teams/organizations)
        List<PlaceResponse> suggestedPlaces = userLocations.stream()
                .filter(loc -> loc.getCategory() != LocationCategory.VOLUNTEER_TEAM && loc.getCategory() != LocationCategory.ORGANIZATION)
                .limit(5)
                .map(this::mapUserLocationToPlaceResponse)
                .collect(Collectors.toList());

        // 🎯 افرقة مقترحة: User locations ONLY of team/organization categories
        List<PlaceResponse> suggestedTeams = userLocations.stream()
                .filter(loc -> loc.getCategory() == LocationCategory.VOLUNTEER_TEAM || loc.getCategory() == LocationCategory.ORGANIZATION)
                .limit(5)
                .map(this::mapUserLocationToPlaceResponse)
                .collect(Collectors.toList());

        // 🎯 1️⃣ التعديل الأول: تصفية الأماكن التاريخية الأثرية بناءً على الفئة الموحدة HISTORICAL
        List<PlaceResponse> historicalPlaces = staticPlaces.stream()
                .filter(p -> p.getCategory() == PlaceCategory.HISTORICAL) 
                .limit(5)
                .map(this::mapStaticToPlaceResponse)
                .collect(Collectors.toList());

        // 🎯 هندسة الفعاليات القادمة (Upcoming Activities)
        List<ActivityResponse> approvedActivities = activityRepository
                .findByStatusInAndGovernorateId(List.of(ActivityStatus.APPROVED, ActivityStatus.POSTPONED), id).stream()
                .filter(activity -> !activity.getIsDeleted())
                .filter(activity -> activity.getLocationId() == null || !finalBlockedLocs.contains(activity.getLocationId()))
                .limit(5)
                .map(activity -> { 
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

        // 🎯 هندسة العروض (Offers)
        List<OfferResponse> approvedOffers = offerRepository
                .findByStatusAndGovernorateId(OfferStatus.ACTIVE, id).stream()
                .filter(offer -> !offer.getIsDeleted())
                .filter(offer -> offer.getLocation() == null || !finalBlockedLocs.contains(offer.getLocation().getId()))
                .limit(5)
                .map(offer -> {
                    List<OfferProductResponse> productDtos = new ArrayList<>();
                    if (offer.getProducts() != null) {
                        for (OfferProduct p : offer.getProducts()) {
                            productDtos.add(OfferProductResponse.builder()
                                    .id(p.getId())
                                    .productName(p.getProductName())
                                    .priceBefore(p.getPriceBefore())
                                    .priceAfter(p.getPriceAfter())
                                    .build());
                        }
                    }

                    String phone = offer.getPhoneNumber() != null ? offer.getPhoneNumber() :
                                   ((offer.getLocation() != null && offer.getLocation().getPhoneNumber() != null) 
                                    ? offer.getLocation().getPhoneNumber() 
                                    : (offer.getCreatedBy() != null ? offer.getCreatedBy().getPhoneNumber() : null));

                    String locPhone = offer.getLocation() != null ? offer.getLocation().getPhoneNumber() : null;

                    return OfferResponse.builder()
                            .id(offer.getId())
                            .title(offer.getTitle())
                            .description(offer.getDescription())
                            .imageUrl(offer.getImageUrl())
                            .scheduleType(offer.getScheduleType())
                            .startDate(offer.getStartDate())
                            .endDate(offer.getEndDate())
                            .startTime(offer.getStartTime())
                            .endTime(offer.getEndTime())
                            .products(productDtos)
                            .locationId(offer.getLocation().getId())
                            .locationName(offer.getLocation().getName())
                            .latitude(offer.getLatitude())
                            .longitude(offer.getLongitude())
                            .governorateId(offer.getGovernorate().getId())
                            .governorateName(offer.getGovernorate().getName())
                            .statusId((long) offer.getStatus().getId())
                            .createdById(offer.getCreatedBy().getId())
                            .createdByUsername(offer.getLocation() != null ? offer.getLocation().getName() : offer.getCreatedBy().getUsername())
                            .phoneNumber(phone)
                            .locationPhone(locPhone)
                            .viewsCount(offer.getViewsCount() != null ? offer.getViewsCount() : 0)
                            .clicksCount(offer.getClicksCount() != null ? offer.getClicksCount() : 0)
                            .build();
                })
                .collect(Collectors.toList());

        GovernorateDetailsResponse details = GovernorateDetailsResponse.builder()
                .id(gov.getId())
                .name(gov.getName())
                .description(gov.getDescription()) 
                .images(imageUrls)
                .suggestedPlaces(suggestedPlaces)       
                .historicalPlaces(historicalPlaces)
                .suggestedTeams(suggestedTeams)
                .upcomingActivities(approvedActivities)   
                .offers(approvedOffers)
                .build();

        return new ApiResponse<>(true, "تم استرجاع تفاصيل المحافظة بنجاح", details);
    }

    private PlaceResponse mapStaticToPlaceResponse(Place place) {
        if (place == null) return null;
        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .imageUrl(place.getImageUrl())
                .category(place.getCategory() != null ? place.getCategory().getLabel() : "اخرى")
                .isUserGenerated(false) 
                .governorateName(place.getGovernorate() != null ? place.getGovernorate().getName() : null)
                .build();
    }

    private PlaceResponse mapUserLocationToPlaceResponse(LocationList loc) {
        if (loc == null) return null;
        return PlaceResponse.builder()
                .id(loc.getId())
                .name(loc.getName())
                .description(loc.getDescription())
                .imageUrl(loc.getCoverUrl() != null ? loc.getCoverUrl() : loc.getLogoUrl()) 
                .category(loc.getCategory() != null ? loc.getCategory().getLabel() : "اخرى")
                .isUserGenerated(true) 
                .logoUrl(loc.getLogoUrl())
                .rating(loc.getRating())
                .governorateName(loc.getGovernorate() != null ? loc.getGovernorate().getName() : null)
                .build();
    }

    public ApiResponse<PlaceResponse> getPlaceDetails(Long placeId) {
        Place place = placeRepository.findById(placeId).orElse(null);
        if (place == null) {
            return new ApiResponse<>(false, "المكان الأثري غير موجود", null);
        }
        return new ApiResponse<>(true, "تم استرجاع تفاصيل المكان بنجاح", mapStaticToPlaceResponse(place));
    }
}
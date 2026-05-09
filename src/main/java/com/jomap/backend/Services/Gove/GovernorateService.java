package com.jomap.backend.Services.Gove;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Events.EventResponse;
import com.jomap.backend.DTOs.Gove.GovernorateDetailsResponse;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.Entities.Events.EventRepository;
import com.jomap.backend.Entities.Events.EventStatus;
import com.jomap.backend.Entities.Gove.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private EventRepository eventRepository;

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

        List<String> imageUrls = gov.getImages().stream()
                .map(GovernorateImage::getImageUrl)
                .collect(Collectors.toList());

        List<Place> places = placeRepository.findByGovernorateId(id);

        Map<String, List<PlaceResponse>> groupedPlaces = places.stream()
                .map(place -> {
                    PlaceResponse response = new PlaceResponse();
                    response.setId(place.getId());
                    response.setName(place.getName());
                    response.setDescription(place.getDescription());
                    response.setImageUrl(place.getImageUrl());
                    response.setCategory(place.getCategory());

                    return response;
                })
                .collect(Collectors.groupingBy(response -> response.getCategory().name()));

        List<EventResponse> approvedEvents = eventRepository
                .findByStatusAndGovernorateId(EventStatus.APPROVED, id).stream()
                .map(event -> {
                    EventResponse response = new EventResponse();
                    response.setId(event.getId());
                    response.setTitle(event.getTitle());
                    response.setDescription(event.getDescription());
                    response.setImageUrl(event.getImageUrl());
                    response.setDate(event.getDate().toString());
                    return response;
                })
                .collect(Collectors.toList());

        // 4. بناء الاستجابة
        GovernorateDetailsResponse details = GovernorateDetailsResponse.builder()
                .id(gov.getId())
                .name(gov.getName())
                .images(imageUrls)
                .placesByCategory(groupedPlaces)
                .events(approvedEvents)
                .build();

        return new ApiResponse<>(true, "تم استرجاع تفاصيل المحافظة بنجاح", details);
    }
}
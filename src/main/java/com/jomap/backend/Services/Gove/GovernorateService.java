package com.jomap.backend.Services.Gove;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Gove.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GovernorateService {

    private final GovernorateRepository governorateRepository;
    private final GovernorateImageRepository imageRepository;
    private final PlaceRepository placeRepository;

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

}
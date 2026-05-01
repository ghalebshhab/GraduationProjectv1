package com.jomap.backend.Services.Gove;

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

    public GovernorateImage addImageToGovernorate(Long governorateId, String imageUrl) {
        Governorate governorate = governorateRepository.findById(governorateId)
                .orElseThrow(() -> new RuntimeException("المحافظة غير موجودة"));

        GovernorateImage image = new GovernorateImage();
        image.setImageUrl(imageUrl);
        image.setGovernorate(governorate);

        return imageRepository.save(image);
    }
    public Place addPlaceToGovernorate(Long governorateId, String name, String description, String imageUrl) {
        Governorate governorate = governorateRepository.findById(governorateId)
                .orElseThrow(() -> new RuntimeException("المحافظة غير موجودة"));

        Place place = new Place();
        place.setName(name);
        place.setDescription(description);
        place.setImageUrl(imageUrl);
        place.setGovernorate(governorate);

        return placeRepository.save(place);
    }

}
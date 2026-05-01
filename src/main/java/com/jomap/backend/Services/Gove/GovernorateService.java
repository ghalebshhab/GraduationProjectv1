package com.jomap.backend.Services.Gove;

import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Gove.GovernorateImage;
import com.jomap.backend.Entities.Gove.GovernorateImageRepository;
import com.jomap.backend.Entities.Gove.GovernorateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GovernorateService {

    private final GovernorateRepository governorateRepository;
    private final GovernorateImageRepository imageRepository;


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
}
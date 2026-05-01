package com.jomap.backend.Controllers.Gov;


import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Gove.GovernorateImage;
import com.jomap.backend.Entities.Gove.Place;
import com.jomap.backend.Services.Gove.GovernorateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/governorates")
@RequiredArgsConstructor
public class GovernorateController {

    private final GovernorateService governorateService;

    @GetMapping
    public ResponseEntity<List<Governorate>> getAll() {
        return ResponseEntity.ok(governorateService.getAllGovernorates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Governorate> getById(@PathVariable Long id) {
        return governorateService.getGovernorateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImageUrl(
            @PathVariable Long id,
            @RequestParam String imageUrl) {
        try {
            GovernorateImage savedImage = governorateService.addImageToGovernorate(id, imageUrl);
            return ResponseEntity.ok(savedImage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{id}/places")
    public ResponseEntity<?> addPlace(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) String imageUrl) {

        try {
            Place savedPlace = governorateService.addPlaceToGovernorate(id, name, description, imageUrl);
            return ResponseEntity.ok(savedPlace);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
package com.jomap.backend.Controllers.Dev;

import com.jomap.backend.Entities.Places.LocationList;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Places.LocationListRepo;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.DTOs.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dev/places")
@RequiredArgsConstructor
public class DevPlaceCleanupController {

    private final UserRepository userRepository;
    private final LocationListRepo placeRepository;

    @DeleteMapping("/duplicates")
    public ApiResponse<String> deleteDuplicatePlaces(@RequestParam String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();

        List<LocationList> places = placeRepository.findAll()
                .stream()
                .filter(place -> place.getOwner() != null)
                .filter(place -> place.getOwner().getId().equals(user.getId()))
                .sorted(Comparator.comparing(LocationList::getId))
                .toList();

        if (places.size() <= 1) {
            return ApiResponse.success("No duplicate places found", "Kept " + places.size() + " place");
        }

        LocationList keptPlace = places.get(0);

        List<LocationList> duplicates = places.subList(1, places.size());

        placeRepository.deleteAll(duplicates);

        return ApiResponse.success(
                "Duplicate places deleted successfully",
                "Kept place id: " + keptPlace.getId() + ", deleted: " + duplicates.size()
        );
    }
}
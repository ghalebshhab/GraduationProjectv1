package com.jomap.backend.Controllers.Dev;

import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.DTOs.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dev/locations")
@RequiredArgsConstructor
public class DevLocationCleanupController {

    private final UserRepository userRepository;
    private final LocationRepo locationRepository;

    @DeleteMapping("/duplicates")
    public ApiResponse<String> deleteDuplicateLocations(@RequestParam String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();

        List<LocationList> locations = locationRepository.findAll()
                .stream()
                .filter(location -> location.getOwner() != null)
                .filter(location -> location.getOwner().getId().equals(user.getId()))
                .sorted(Comparator.comparing(LocationList::getId))
                .toList();

        if (locations.size() <= 1) {
            return ApiResponse.success("No duplicate locations found", "Kept " + locations.size() + " location");
        }

        LocationList keptLocation = locations.get(0);
        List<LocationList> duplicates = locations.subList(1, locations.size());

        locationRepository.deleteAll(duplicates);

        return ApiResponse.success(
                "تم حذف المواقع المكررة بنجاح",
                "تم الإبقاء على الموقع رقم: " + keptLocation.getId() + "، وتم حذف: " + duplicates.size() + " مواقع مكررة."
        );
    }
}
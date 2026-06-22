package com.jomap.backend.Controllers.Fav;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Fav.FavoritesDataDto;
import com.jomap.backend.Services.Fav.FavoritesService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@AllArgsConstructor
public class FavoritesController {

    private final FavoritesService favoritesService;

    @GetMapping
    public ResponseEntity<ApiResponse<FavoritesDataDto>> getMyFavorites(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<FavoritesDataDto> response = favoritesService.getMyFavorites(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/locations/{id}")
    public ResponseEntity<ApiResponse<Void>> addFavoriteLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.addFavoriteLocation(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteLocation(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.removeFavoriteLocation(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/places/{id}")
    public ResponseEntity<ApiResponse<Void>> addFavoritePlace(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.addFavoritePlace(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/places/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFavoritePlace(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.removeFavoritePlace(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/events/{id}", "/activities/{id}"})
    public ResponseEntity<ApiResponse<Void>> addFavoriteActivity(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.addFavoriteActivity(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping({"/events/{id}", "/activities/{id}"})
    public ResponseEntity<ApiResponse<Void>> removeFavoriteActivity(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.error("المستخدم غير موثق"));
        }

        ApiResponse<Void> response = favoritesService.removeFavoriteActivity(id, principal.getName());
        return ResponseEntity.ok(response);
    }
}

package com.jomap.backend.Controllers.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jomap.backend.Services.search.SearchService;

import lombok.AllArgsConstructor;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@RestController
@RequestMapping("/api/search")
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/getall")
    public ResponseEntity<?> getAllItems(
            org.springframework.security.core.Authentication authentication,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(searchService.getAllItems(email, page, size));
    }

}
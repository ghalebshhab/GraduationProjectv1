package com.jomap.backend.Controllers.Post;
import com.jomap.backend.DTOs.Posts.SavedPosts.CreateSavedPostRequest;
import com.jomap.backend.Services.Community.Posts.SavedPosts.SavedPostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts/saved")
@CrossOrigin(origins = "http://localhost:5173")
public class SavedPostController {

    private final SavedPostService savedPostService;

    public SavedPostController(SavedPostService savedPostService) {
        this.savedPostService = savedPostService;
    }

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<?> toggleSave(@PathVariable Long postId, Principal principal) {
        return savedPostService.toggleSave(postId, principal.getName());
    }

    @GetMapping("/status/{postId}")
    public ResponseEntity<?> isPostSaved(@PathVariable Long postId, Principal principal) {
        return savedPostService.isPostSaved(postId, principal.getName());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySavedPosts(Principal principal) {
        return savedPostService.getMySavedPosts(principal.getName());
    }
}
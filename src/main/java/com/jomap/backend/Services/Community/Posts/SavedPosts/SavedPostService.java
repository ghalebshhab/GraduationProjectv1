package com.jomap.backend.Services.Community.Posts.SavedPosts;
import com.jomap.backend.DTOs.Posts.SavedPosts.CreateSavedPostRequest;
import org.springframework.http.ResponseEntity;

public interface SavedPostService {

    ResponseEntity<?> toggleSave(Long postId, String email);
    ResponseEntity<?> isPostSaved(Long postId, String email);
    ResponseEntity<?> getMySavedPosts(String email);
}
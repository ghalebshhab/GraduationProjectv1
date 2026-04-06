package com.jomap.backend.Services.Community.Posts.SavedPosts;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.SavedPosts.CreateSavedPostRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SavedPostService {

    ApiResponse<Map<String, Object>> toggleSave(Long postId, String emailFromToken);

    ApiResponse<Map<String, Boolean>> isPostSaved(Long postId, String emailFromToken);

    ApiResponse<List<PostResponse>> getMySavedPosts(String emailFromToken);
}
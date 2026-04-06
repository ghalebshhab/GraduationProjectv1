package com.jomap.backend.Services.Community.Posts.Likes;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Likes.PostLikeResponse;

import java.util.Map;

public interface PostLikeService {
    ApiResponse<Long> countByPostId(Long postId);
    ApiResponse<Map<String, Boolean>> existsByPostId(String emailFromToken, Long postId);
    ApiResponse<PostLikeResponse> addLike(String emailFromToken, Long postId);
    ApiResponse<String> deleteByPostId(String emailFromToken, Long postId);
}
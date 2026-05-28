package com.jomap.backend.Services.Community.Posts;

import java.util.List;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;

public interface PostsServices {


        ApiResponse<List<PostResponse>> getAllPosts(int userid, String Category);

        ApiResponse<PostResponse> getPostById(Long postId);

        ApiResponse<List<PostResponse>> getFeedSummary(int page, int size);

        ApiResponse<PostResponse> createPost(String emailFromToken, CreatePostRequest request);

        ApiResponse<PostResponse> updatePost(String emailFromToken, Long postId, UpdatePostRequest request);

        ApiResponse<String> deletePost(String emailFromToken, Long postId);

        ApiResponse<List<PostResponse>> getPersonalizedFeed(String emailFromToken, double userLat, double userLng, int page, int size);

        ApiResponse<List<PostResponse>> getMyPosts(String emailFromToken);

        ApiResponse<List<PostResponse>> getUserPosts(Long userId);
}
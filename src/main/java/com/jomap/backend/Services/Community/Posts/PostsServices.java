package com.jomap.backend.Services.Community.Posts;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PostsServices {


        ApiResponse<List<PostResponse>> getAllPosts();

        ApiResponse<PostResponse> getPostById(Long postId);

        ApiResponse<List<PostResponse>> getFeedSummary(int page, int size);

        ApiResponse<PostResponse> createPost(String emailFromToken, CreatePostRequest request);

        ApiResponse<PostResponse> updatePost(String emailFromToken, Long postId, UpdatePostRequest request);

        ApiResponse<String> deletePost(String emailFromToken, Long postId);

}
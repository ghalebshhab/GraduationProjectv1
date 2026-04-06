package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.jomap.backend.Entities.Posts.postComments.PostComment;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface PostCommentService {

    ApiResponse<Long> countByPostId(Long postId);

    ApiResponse<PostCommentResponse> getCommentById(Long commentId);

    ApiResponse<List<PostCommentResponse>> getCommentsByPostId(Long postId);

    ApiResponse<PostCommentResponse> addComment(String emailFromToken, Long postId, CreatePostCommentRequest request);

    ApiResponse<PostCommentResponse> updateComment(String emailFromToken, Long commentId, UpdatePostCommentRequest request);

    ApiResponse<String> deleteComment(String emailFromToken, Long commentId);
}
package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;

import java.util.List;
import java.util.Map;

public interface PostCommentService {

    // ── Called by PostCommentsController ─────────────────────────────────────

    ApiResponse<List<PostCommentResponse>> getCommentsByPostId(Long postId);

    ApiResponse<PostCommentResponse> getCommentById(Long commentId);

    ApiResponse<Long> countByPostId(Long postId);

    ApiResponse<PostCommentResponse> addComment(String emailFromToken, Long postId,
                                                CreatePostCommentRequest request);

    ApiResponse<PostCommentResponse> updateComment(String emailFromToken, Long commentId,
                                                   UpdatePostCommentRequest request);

    ApiResponse<String> deleteComment(String emailFromToken, Long commentId);

    // ── Called internally by PostServiceImpl (feed algorithm) ────────────────

    /** Returns all distinct post IDs this user has commented on. */
    List<Long> getPostIdsCommentedByUser(Long userId);

    /** Bulk comment counts for a list of post IDs — avoids N+1 in the feed. */
    Map<Long, Long> countByPostIds(List<Long> postIds);
}
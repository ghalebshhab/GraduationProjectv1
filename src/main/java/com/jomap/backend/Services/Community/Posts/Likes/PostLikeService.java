package com.jomap.backend.Services.Community.Posts.Likes;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Likes.PostLikeResponse;

import java.util.List;
import java.util.Map;

public interface PostLikeService {

    // ── Called by PostLikesController ─────────────────────────────────────────

    ApiResponse<Long> countByPostId(Long postId);

    ApiResponse<Map<String, Boolean>> existsByPostId(String emailFromToken, Long postId);

    ApiResponse<PostLikeResponse> addLike(String emailFromToken, Long postId);

    ApiResponse<String> deleteByPostId(String emailFromToken, Long postId);

    // ── Called internally by PostServiceImpl (feed algorithm) ─────────────────

    /** Returns all post IDs this user has liked. */
    List<Long> getPostIdsLikedByUser(Long userId);

    /** Bulk like counts for a list of post IDs — avoids N+1 in the feed. */
    Map<Long, Long> countByPostIds(List<Long> postIds);
}
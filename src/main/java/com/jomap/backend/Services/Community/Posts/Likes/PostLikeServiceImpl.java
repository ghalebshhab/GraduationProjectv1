package com.jomap.backend.Services.Community.Posts.Likes;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Likes.PostLikeResponse;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Posts.postLikes.PostLikes;
import com.jomap.backend.Entities.Posts.postLikes.PostLikesRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikesRepository postLikesRepository;
    private final PostRepository      postRepository;
    private final UserRepository      userRepository;

    // ── Count likes for a single post ─────────────────────────────────────────

    @Override
    public ApiResponse<Long> countByPostId(Long postId) {
        long count = postLikesRepository.countByPostId(postId);
        return ApiResponse.success("Like count fetched", count);
    }

    // ── Check if the current user has already liked a post ────────────────────
    // Returns Map<"liked", true/false> — matches the controller's
    // ResponseEntity<ApiResponse<Map<String, Boolean>>> signature.

    @Override
    public ApiResponse<Map<String, Boolean>> existsByPostId(String emailFromToken, Long postId) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        boolean liked = postLikesRepository.existsByPostIdAndUserId(postId, user.getId());
        return ApiResponse.success("Like status fetched", Map.of("liked", liked));
    }

    // ── Add a like ────────────────────────────────────────────────────────────
    // Idempotent: if the user already liked the post, returns the existing like.

    @Override
    @Transactional
    public ApiResponse<PostLikeResponse> addLike(String emailFromToken, Long postId) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        // Check for duplicate — return existing like rather than throwing an error
        return postLikesRepository.findByPostIdAndUserId(postId, user.getId())
                .map(existing -> ApiResponse.success("Already liked", toResponse(existing)))
                .orElseGet(() -> {
                    PostLikes saved = postLikesRepository.save(new PostLikes(post, user));
                    return ApiResponse.success("Post liked successfully", toResponse(saved));
                });
    }

    // ── Delete a like ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<String> deleteByPostId(String emailFromToken, Long postId) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        return postLikesRepository.findByPostIdAndUserId(postId, user.getId())
                .map(like -> {
                    postLikesRepository.delete(like);
                    return ApiResponse.success("Post unliked successfully", "unliked");
                })
                .orElse(ApiResponse.error("Like not found — you haven't liked this post"));
    }

    // ── Feed algorithm helpers ────────────────────────────────────────────────

    @Override
    public List<Long> getPostIdsLikedByUser(Long userId) {
        List<Long> ids = postLikesRepository.findPostIdsLikedByUser(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postLikesRepository.countLikesByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    // ── Response mapper ───────────────────────────────────────────────────────

    private PostLikeResponse toResponse(PostLikes like) {
        PostLikeResponse r = new PostLikeResponse();
        r.setId(like.getId());
        r.setPostId(like.getPost() != null ? like.getPost().getId() : null);
        r.setCreatedAt(like.getCreatedAt());

        if (like.getUser() != null) {
            r.setUserId(like.getUser().getId());
            r.setUsername(like.getUser().getUsername());
        }

        return r;
    }
}
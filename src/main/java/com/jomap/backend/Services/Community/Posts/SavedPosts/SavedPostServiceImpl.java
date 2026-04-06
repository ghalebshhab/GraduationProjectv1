package com.jomap.backend.Services.Community.Posts.SavedPosts;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPost;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPostsRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SavedPostServiceImpl implements SavedPostService {

    private final SavedPostsRepository savedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> toggleSave(Long postId, String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        Optional<SavedPost> existing = savedPostRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            savedPostRepository.delete(existing.get());

            return ApiResponse.success(
                    "Post unsaved successfully",
                    Map.of(
                            "saved", false,
                            "postId", postId
                    )
            );
        }

        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);

        savedPostRepository.save(savedPost);

        return ApiResponse.success(
                "Post saved successfully",
                Map.of(
                        "saved", true,
                        "postId", postId
                )
        );
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Boolean>> isPostSaved(Long postId, String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        boolean saved = savedPostRepository.existsByUserIdAndPostId(user.getId(), postId);

        return ApiResponse.success(
                "Saved status fetched successfully",
                Map.of("saved", saved)
        );
    }

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getMySavedPosts(String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        List<PostResponse> responses = savedPostRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(SavedPost::getPost)
                .filter(post -> post != null && !Boolean.TRUE.equals(post.getIsDeleted()))
                .map(this::mapToPostResponse)
                .toList();

        return ApiResponse.success("Saved posts fetched successfully", responses);
    }

    private PostResponse mapToPostResponse(Post post) {
        PostResponse response = new PostResponse();

        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setMediaUrl(post.getMediaUrl());
        response.setType(post.getType() != null ? post.getType().name() : null);
        response.setCreatedAt(post.getCreatedAt());

        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorEmail(post.getAuthor().getEmail());
            response.setAuthorUsername(post.getAuthor().getUsername());
            response.setAuthorProfileImageUrl(post.getAuthor().getProfileImageUrl());
        }

        return response;
    }
}
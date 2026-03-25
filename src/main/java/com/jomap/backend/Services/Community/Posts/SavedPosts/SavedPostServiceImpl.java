package com.jomap.backend.Services.Community.Posts.SavedPosts;
import com.jomap.backend.DTOs.Posts.SavedPosts.CreateSavedPostRequest;
import com.jomap.backend.DTOs.Posts.SavedPosts.SavedPostResponse;
import com.jomap.backend.DTOs.Posts.SavedPosts.SavedPostStatusResponse;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPost;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPostsRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SavedPostServiceImpl implements SavedPostService {

    private final SavedPostsRepository savedPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;



    @Override
    @Transactional
    public ResponseEntity<?> toggleSave(Long postId, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
            ));
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Post not found"
            ));
        }

        SavedPost existing = savedPostRepository
                .findByUserIdAndPostId(user.getId(), postId)
                .orElse(null);

        if (existing != null) {
            savedPostRepository.delete(existing);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post unsaved successfully",
                    "saved", false
            ));
        }

        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);
        savedPostRepository.save(savedPost);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post saved successfully",
                "saved", true
        ));
    }

    @Override
    public ResponseEntity<?> isPostSaved(Long postId, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
            ));
        }

        boolean saved = savedPostRepository.existsByUserIdAndPostId(user.getId(), postId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "postId", postId,
                "saved", saved
        ));
    }

    @Override
    public ResponseEntity<?> getMySavedPosts(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
            ));
        }

        List<SavedPost> savedPosts = savedPostRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<SavedPostResponse> data = savedPosts.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", data.size(),
                "data", data
        ));
    }

    private SavedPostResponse mapToResponse(SavedPost savedPost) {
        SavedPostResponse response = new SavedPostResponse();
        response.setId(savedPost.getId());
        response.setUserId(savedPost.getUser().getId());
        response.setSavedAt(savedPost.getCreatedAt());

        Post post = savedPost.getPost();
        response.setPostId(post.getId());
        response.setContent(post.getContent());
        response.setMediaUrl(post.getMediaUrl());
        response.setType(post.getType() != null ? post.getType().name() : null);
        response.setPostCreatedAt(post.getCreatedAt());

        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorUsername(post.getAuthor().getUsername());
        }

        return response;
    }
}
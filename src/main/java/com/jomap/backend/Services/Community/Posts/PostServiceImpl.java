package com.jomap.backend.Services.Community.Posts;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Community.Posts.Comments.PostCommentService;
import com.jomap.backend.Services.Community.Posts.Likes.PostLikeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
@AllArgsConstructor
public class PostServiceImpl implements PostsServices{

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeService likesService;
    private final PostCommentService commentsService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PostResponse>> getAllPosts() {
        List<PostResponse> responses = postRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(post -> !Boolean.TRUE.equals(post.getIsDeleted()))
                .map(this::toResponse)
                .toList();

        return ApiResponse.success("Posts fetched successfully", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PostResponse> getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        return ApiResponse.success("Post fetched successfully", toResponse(post));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PostResponse>> getFeedSummary(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<PostResponse> responses = postRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(post -> !Boolean.TRUE.equals(post.getIsDeleted()))
                .map(this::toResponse)
                .toList();

        return ApiResponse.success("Feed fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> createPost(String emailFromToken, CreatePostRequest request) {
        User author = userRepository.findByEmail(emailFromToken).orElse(null);
        if (author == null) {
            return ApiResponse.error("User not found");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Content is required");
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent().trim());
        post.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid post type");
            }
        } else {
            post.setType(Post.PostType.COMMUNITY);
        }

        Post saved = postRepository.save(post);
        return ApiResponse.success("Post created successfully", toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> updatePost(String emailFromToken, Long postId, UpdatePostRequest request) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only update your own post");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Content is required");
        }

        post.setContent(request.getContent().trim());
        post.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid post type");
            }
        }

        Post saved = postRepository.save(post);
        return ApiResponse.success("Post updated successfully", toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse<String> deletePost(String emailFromToken, Long postId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only delete your own post");
        }

        post.setIsDeleted(true);
        postRepository.save(post);

        return ApiResponse.success("Post deleted successfully", "Post deleted successfully");
    }

    private PostResponse toResponse(Post post) {
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

        response.setLikeCount(likesService.countByPostId(post.getId()).getData());
        response.setCommentCount(commentsService.countByPostId(post.getId()).getData());

        return response;
    }
}
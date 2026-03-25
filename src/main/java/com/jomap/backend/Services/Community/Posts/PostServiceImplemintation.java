package com.jomap.backend.Services.Community.Posts;

import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Community.Posts.Comments.CommentsServices;
import com.jomap.backend.Services.Community.Posts.Likes.LikesService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PostServiceImplemintation implements PostsServices {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikesService likesService;
    private final CommentsServices commentsService;

    @Override
    public ResponseEntity<?> createPost(CreatePostRequest request) {

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User author = optionalUser.get();

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Content is required"));
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid post type: " + request.getType()));
            }
        } else {
            post.setType(Post.PostType.COMMUNITY);
        }

        Post saved = postRepository.save(post);
        return ResponseEntity.ok(toResponse(saved));
    }

    @Override
    public ResponseEntity<?> update(Long postId, UpdatePostRequest request) {

        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty() || Boolean.TRUE.equals(optionalPost.get().getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Post not found with id: " + postId));
        }

        Post existing = optionalPost.get();

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        if (!existing.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only update your own post"));
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Content is required"));
        }

        existing.setContent(request.getContent());
        existing.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                existing.setType(Post.PostType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid post type: " + request.getType()));
            }
        }

        Post saved = postRepository.save(existing);
        return ResponseEntity.ok(toResponse(saved));
    }

    @Override
    public ResponseEntity<?> findResponseById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty() || Boolean.TRUE.equals(optionalPost.get().getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Post not found with id: " + postId));
        }

        return ResponseEntity.ok(toResponse(optionalPost.get()));
    }

    @Override
    public List<PostResponse> findAllResponses() {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostResponse> res = new ArrayList<>();

        for (Post p : posts) {
            if (!Boolean.TRUE.equals(p.getIsDeleted())) {
                res.add(toResponse(p));
            }
        }

        return res;
    }

    @Override
    public List<PostResponse> feedSummary(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var postsPage = postRepository.findAll(pageable);

        List<PostResponse> result = new ArrayList<>();
        for (Post p : postsPage.getContent()) {
            if (!Boolean.TRUE.equals(p.getIsDeleted())) {
                result.add(toResponse(p));
            }
        }
        return result;
    }

    @Override
    public ResponseEntity<?> deletePost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty() || Boolean.TRUE.equals(optionalPost.get().getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Post not found with id: " + postId));
        }

        Post post = optionalPost.get();

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only delete your own post"));
        }

        post.setIsDeleted(true);
        postRepository.save(post);

        return ResponseEntity.ok(
                Map.of("message", "Post deleted successfully"));
    }

    private PostResponse toResponse(Post post) {
        PostResponse r = new PostResponse();
        r.setId(post.getId());
        r.setContent(post.getContent());
        r.setMediaUrl(post.getMediaUrl());
        r.setType(post.getType() != null ? post.getType().name() : null);
        r.setCreatedAt(post.getCreatedAt());

        if (post.getAuthor() != null) {
            r.setAuthorId(post.getAuthor().getId());
            r.setAuthorEmail(post.getAuthor().getEmail());
            r.setAuthorUsername(post.getAuthor().getUsername());
            r.setAuthorProfileImageUrl(post.getAuthor().getProfileImageUrl());
        }

        r.setLikeCount(likesService.countByPostId(post.getId()));
        r.setCommentCount(commentsService.countByPostId(post.getId()));

        return r;
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isEmpty() || Boolean.TRUE.equals(optionalPost.get().getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Post not found with id: " + id));
        }

        return ResponseEntity.ok(optionalPost.get());
    }

    @Override
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email);
    }
}
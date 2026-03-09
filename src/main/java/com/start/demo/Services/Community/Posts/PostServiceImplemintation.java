package com.start.demo.Services.Community.Posts;

import com.start.demo.DTOs.Posts.CreatePostRequest;
import com.start.demo.DTOs.Posts.PostResponse;
import com.start.demo.DTOs.Posts.UpdatePostRequest;
import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.PostRepository;
import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import com.start.demo.Exciptions.BadRequestException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import com.start.demo.Services.Community.Posts.Comments.CommentsServices;
import com.start.demo.Services.Community.Posts.Likes.LikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImplemintation implements PostsServices {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikesService likesService;
    private final CommentsServices commentsService;

    @Autowired
    public PostServiceImplemintation(PostRepository postRepository,
                                     UserRepository userRepository,
                                     LikesService likesService,
                                     CommentsServices commentsService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likesService = likesService;
        this.commentsService = commentsService;
    }

    @Override
    public PostResponse create(CreatePostRequest request) {

        User author = getCurrentUser();

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Content is required");
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid post type: " + request.getType());
            }
        } else {
            post.setType(Post.PostType.COMMUNITY);
        }

        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    @Override
    public PostResponse update(Long postId, UpdatePostRequest request) {

        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(existing.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User currentUser = getCurrentUser();

        if (!existing.getAuthor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own post");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Content is required");
        }

        existing.setContent(request.getContent());
        existing.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                existing.setType(Post.PostType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid post type: " + request.getType());
            }
        }

        Post saved = postRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    public PostResponse findResponseById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return toResponse(post);
    }

    @Override
    public List<PostResponse> findAllResponses() {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PostResponse> res = new ArrayList<>();

        for (Post p : posts) {
            if (!Boolean.TRUE.equals(p.getDeleted())) {
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
            if (!Boolean.TRUE.equals(p.getDeleted())) {
                result.add(toResponse(p));
            }
        }
        return result;
    }

    @Override
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User currentUser = getCurrentUser();

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own post");
        }

        post.setDeleted(true);
        postRepository.save(post);
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
    public Post findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (Boolean.TRUE.equals(post.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }

        return post;
    }

    @Override
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Authenticated user not found");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
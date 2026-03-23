package com.jomap.backend.Services.Community.Posts;

import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PostsServices {

    ResponseEntity<?> createPost(CreatePostRequest request);

    ResponseEntity<?> update(Long postId, UpdatePostRequest request);

    ResponseEntity<?> findResponseById(Long postId);

    List<PostResponse> findAllResponses();

    List<PostResponse> feedSummary(int page, int size);

    ResponseEntity<?> deletePost(Long postId);

    List<Post> findAll();

    ResponseEntity<?> findById(Long id);

    Post savePost(Post post);
}
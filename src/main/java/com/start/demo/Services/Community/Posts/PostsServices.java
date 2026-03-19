package com.start.demo.Services.Community.Posts;

import com.start.demo.DTOs.Posts.CreatePostRequest;
import com.start.demo.DTOs.Posts.PostResponse;
import com.start.demo.DTOs.Posts.UpdatePostRequest;
import com.start.demo.Entities.Posts.Post;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PostsServices {

    ResponseEntity<?> create(CreatePostRequest request);

    ResponseEntity<?> update(Long postId, UpdatePostRequest request);

    ResponseEntity<?> findResponseById(Long postId);

    List<PostResponse> findAllResponses();

    List<PostResponse> feedSummary(int page, int size);

    ResponseEntity<?> deletePost(Long postId);

    List<Post> findAll();

    ResponseEntity<?> findById(Long id);

    Post savePost(Post post);
}
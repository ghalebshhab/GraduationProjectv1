package com.start.demo.Services.Community.Posts;

import com.start.demo.Entities.Posts.Post;

import java.util.List;
import java.util.Map;

import com.start.demo.DTOs.Posts.CreatePostRequest;
import com.start.demo.DTOs.Posts.UpdatePostRequest;
import com.start.demo.DTOs.Posts.PostResponse;
import com.start.demo.Entities.Posts.Post;

import java.util.List;

public interface PostsServices {

    // -------- DTO-based methods (used by controller) --------

    PostResponse create(CreatePostRequest request);

    PostResponse update(Long postId, UpdatePostRequest request);

    PostResponse findResponseById(Long postId);

    List<PostResponse> findAllResponses();

    List<PostResponse> feedSummary(int page, int size);

    void deletePost(Long postId);

    // -------- Optional: keep if used somewhere else --------

    List<Post> findAll();

    Post findById(Long id);

    Post savePost(Post post);
}
package com.start.demo.Services.Posts;

import com.start.demo.Entities.Posts.Post;

import java.util.List;
import java.util.Map;

public interface PostsServices {
    List<Post> findAll();
    Post findById(Long id);
    Post savePost(Post post);
    void deletePost(Long postId);
    List<Map<String, Object>> feedSummary(int page, int size);
}

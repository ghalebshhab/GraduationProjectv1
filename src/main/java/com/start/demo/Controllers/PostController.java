package com.start.demo.Controllers;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Services.Posts.PostsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    PostsServices postServices;
    @Autowired
    public PostController(PostsServices postService){
        this.postServices=postService;
    }
    @GetMapping()
    public List<Post> findAll(){
        return postServices.findAll();
    }
    @GetMapping("/{postId}")
    public Post findById(@PathVariable Long postId){
        return postServices.findById(postId);
    }
    @GetMapping("/feed/summary")
    public List<Map<String, Object>> feedSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postServices.feedSummary(page, size);
    }
    @PostMapping()
    public Post createPost(@RequestBody Post post){
        post.setId(null);
        Post thePost=postServices.savePost(post);
        return thePost;
    }
    @PutMapping("/{postId}")
    public Post editPost(@RequestBody Post post,@PathVariable Long postId){
        Post foundedPost=postServices.findById(postId);
        if(foundedPost==null)
            throw new RuntimeException("The post with the id - "+ postId+"is no founded");
        foundedPost.setContent(post.getContent());
        foundedPost.setMediaUrl(post.getMediaUrl());
        foundedPost.setType(post.getType());
        return postServices.savePost(foundedPost);
    }
    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId){
        postServices.deletePost(postId);
        return "The post with the id -"+postId+"is deleted" ;
    }

}

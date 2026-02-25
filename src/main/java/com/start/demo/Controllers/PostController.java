package com.start.demo.Controllers;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Services.Posts.PostsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

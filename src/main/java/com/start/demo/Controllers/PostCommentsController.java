package com.start.demo.Controllers;

import com.start.demo.Entities.Posts.postComments.PostComment;
import com.start.demo.Services.Posts.Comments.CommentsServices;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostCommentsController {
    CommentsServices service;
    public PostCommentsController(CommentsServices service){
        this.service=service;
    }
    @GetMapping("/posts/{postId}/comments")
    public List<PostComment> findAll(@PathVariable Long postId){
        return service.findByPostId(postId);
    }
    @GetMapping("/posts/comments/{commentId}")
    public PostComment findById(@PathVariable Long commentId){
        return service.findById(commentId);
    }
    @GetMapping("/posts/{postId}/comments/count")
    public Long countByPostId(@PathVariable Long postId){
        return service.countByPostId(postId);
    }
    @PostMapping("posts/{postId}/comments")
    public PostComment addComment(@RequestBody PostComment comment,@PathVariable Long postId){
        comment.setPostId(postId);
       return service.addComment(comment);
    }
    @PutMapping("/posts/comments/{commentId}")
    public PostComment editComment(@RequestBody PostComment comment,@PathVariable Long commentId){
        return service.updateComment(commentId,comment);
    }
    @DeleteMapping("/posts/comments/{commentId}")
    public String deleteComment(@PathVariable Long commentId){
        return service.deleteComment(commentId);
    }

}

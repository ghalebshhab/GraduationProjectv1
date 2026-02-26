package com.start.demo.Controllers;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Services.Posts.Likes.LikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/posts/likes")
public class PostLikesController {

    private final LikesService likes;

    @Autowired
    public PostLikesController(LikesService likes) {
        this.likes = likes;
    }

    @GetMapping("/{postId}")
    public Long likesCount(@PathVariable Long postId) {
        return likes.countById(postId);
    }

    @PostMapping
    public String addLike(@RequestBody PostLikes postLikes) {
        // ترجع رسالة بسيطة أو ترجع PostLikes بعد الحفظ
        return likes.addLike(postLikes.getPostId(), postLikes.getUserKey());
    }

    @GetMapping("/exist")
    public Boolean existByUserKeyAndPostId(
            @RequestParam Long postId,
            @RequestParam String userKey
    ) {
        return likes.existsByPostIdAndUserKey(postId, userKey);
    }

    @DeleteMapping
    public String deleteByUserKeyAndPostId(
            @RequestParam Long postId,
            @RequestParam String userKey
    ) {
        return likes.deleteByPostIdAndUserKey(postId, userKey);
    }
}
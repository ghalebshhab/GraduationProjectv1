package com.start.demo.Services.Posts;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.PostRepository;
import com.start.demo.Entities.Users.UserRepository;
import com.start.demo.Services.Posts.Comments.CommentsServices;
import com.start.demo.Services.Posts.Likes.LikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

@Service
public class PostServiceImplemintation implements PostsServices {

    private final PostRepository postRepository;
    private final LikesService likesService;
    private final CommentsServices commentsService;

    @Autowired
    public PostServiceImplemintation(PostRepository postRepository,
                                     LikesService likesService,
                                     CommentsServices commentsService) {
        this.postRepository = postRepository;
        this.likesService = likesService;
        this.commentsService = commentsService;
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("The Post is not found by id-" + id));
    }

    @Override
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    // ✅ NEW: feed summary
    @Override
    public List<Map<String, Object>> feedSummary(int page, int size) {

        // sort newest first
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var postsPage = postRepository.findAll(pageable);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Post p : postsPage.getContent()) {
            Long likeCount = likesService.countById(p.getId());
            Long commentCount = commentsService.countByPostId(p.getId());

            Map<String, Object> row = new HashMap<>();
            row.put("post", p);
            row.put("likeCount", likeCount);
            row.put("commentCount", commentCount);

            result.add(row);
        }

        return result;
    }
}

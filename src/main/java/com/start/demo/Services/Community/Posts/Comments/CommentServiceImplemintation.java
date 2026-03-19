package com.start.demo.Services.Community.Posts.Comments;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postComments.PostComment;
import com.start.demo.Entities.Users.User;
import com.start.demo.Services.Auth.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImplemintation implements CommentsServices {

    @PersistenceContext
    private EntityManager entity;

    private final CurrentUserService currentUserService;

    public CommentServiceImplemintation(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public Long countByPostId(Long postId) {
        if (postId == null || postId <= 0) {
            return 0L;
        }

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.isDeleted = false",
                Long.class
        );
        q.setParameter("postId", postId);
        return q.getSingleResult();
    }

    @Override
    public PostComment findById(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return null;
        }

        PostComment comment = entity.find(PostComment.class, commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getDeleted())) {
            return null;
        }
        return comment;
    }

    @Override
    public List<PostComment> findByPostId(Long postId) {
        TypedQuery<PostComment> q = entity.createQuery(
                "FROM PostComment c WHERE c.post.id = :postId AND c.isDeleted = false ORDER BY c.createdAt DESC",
                PostComment.class
        );
        q.setParameter("postId", postId);
        return q.getResultList();
    }

    @Override
    @Transactional
    public PostComment addComment(Long postId, String content) {
        if (postId == null || postId <= 0 || content == null || content.isBlank()) {
            return null;
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            return null;
        }

        User user = getCurrentUser();
        if (user == null) {
            return null;
        }

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);

        entity.persist(comment);
        return comment;
    }

    @Override
    @Transactional
    public PostComment updateComment(Long commentId, String content) {
        PostComment existing = findById(commentId);
        if (existing == null || content == null || content.isBlank()) {
            return null;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null || existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            return null;
        }

        existing.setContent(content);
        return existing;
    }

    @Override
    @Transactional
    public String deleteComment(Long commentId) {
        PostComment existing = findById(commentId);
        if (existing == null) {
            return null;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null || existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            return null;
        }

        existing.setDeleted(true);
        return "Comment deleted successfully";
    }

    private User getCurrentUser() {
        try {
            return currentUserService.getCurrentUser();
        } catch (Exception ignored) {
            return null;
        }
    }
}

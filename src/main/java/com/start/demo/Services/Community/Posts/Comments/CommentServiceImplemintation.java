package com.start.demo.Services.Community.Posts.Comments;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postComments.PostComment;
import com.start.demo.Entities.Users.User;
import com.start.demo.Exciptions.BadRequestException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImplemintation implements CommentsServices {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public Long countByPostId(Long postId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(c) FROM PostComment c " +
                        "WHERE c.post.id = :postId AND c.isDeleted = false",
                Long.class
        );
        q.setParameter("postId", postId);
        return q.getSingleResult();
    }

    @Override
    public PostComment findById(Long commentId) {
        PostComment c = entity.find(PostComment.class, commentId);
        if (c == null || Boolean.TRUE.equals(c.getDeleted())) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        return c;
    }

    @Override
    public List<PostComment> findByPostId(Long postId) {
        TypedQuery<PostComment> q = entity.createQuery(
                "FROM PostComment c " +
                        "WHERE c.post.id = :postId AND c.isDeleted = false " +
                        "ORDER BY c.createdAt DESC",
                PostComment.class
        );
        q.setParameter("postId", postId);
        return q.getResultList();
    }

    @Override
    @Transactional
    public PostComment addComment(Long postId, String content) {

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Content is required");
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User user = getCurrentUser();

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

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Content is required");
        }

        User currentUser = getCurrentUser();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only edit your own comment");
        }

        existing.setContent(content);
        return existing;
    }

    @Override
    @Transactional
    public String deleteComment(Long commentId) {

        PostComment existing = findById(commentId);
        User currentUser = getCurrentUser();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own comment");
        }

        existing.setDeleted(true);

        return "The comment with the id - " + commentId + " is deleted";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Authenticated user not found");
        }

        String email = authentication.getName();

        TypedQuery<User> query = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        query.setParameter("email", email);

        List<User> users = query.getResultList();

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        return users.get(0);
    }
}
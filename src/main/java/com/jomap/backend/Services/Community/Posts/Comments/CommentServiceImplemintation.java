package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> findById(Long commentId) {
        PostComment c = entity.find(PostComment.class, commentId);

        if (c == null || Boolean.TRUE.equals(c.getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment not found"));
        }

        return ResponseEntity.ok(c);
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
    public ResponseEntity<?> addComment(Long postId, String content) {

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment cannot be empty"));
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Post not found"));
        }

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User user = optionalUser.get();

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);

        entity.persist(comment);

        return ResponseEntity.ok(comment);
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateComment(Long commentId, String content) {

        PostComment existing = entity.find(PostComment.class, commentId);

        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment not found"));
        }

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Content is required"));
        }

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only edit your own comment"));
        }

        existing.setContent(content);

        return ResponseEntity.ok(existing);
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteComment(Long commentId) {

        PostComment existing = entity.find(PostComment.class, commentId);

        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment not found"));
        }

        Optional<User> optionalUser = getCurrentUser();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only delete your own comment"));
        }

        existing.setIsDeleted(true);

        return ResponseEntity.ok(
                Map.of("message", "The comment with id " + commentId + " was deleted")
        );
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }

        String email = authentication.getName();

        TypedQuery<User> query = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        query.setParameter("email", email);

        List<User> users = query.getResultList();

        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.get(0));
    }
}
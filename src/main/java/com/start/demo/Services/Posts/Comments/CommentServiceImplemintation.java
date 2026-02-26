package com.start.demo.Services.Posts.Comments;

import com.start.demo.Entities.Posts.postComments.CommentsRepository;
import com.start.demo.Entities.Posts.postComments.PostComment;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CommentServiceImplemintation implements CommentsServices {

    private final EntityManager entity;

    @Autowired
    public CommentServiceImplemintation(EntityManager entity) {
        this.entity = entity;
    }

    @Override
    public Long countByPostId(Long postId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(c) FROM PostComment c WHERE c.postId = :postId AND c.isDeleted = false",
                Long.class
        );
        q.setParameter("postId", postId);
        return q.getSingleResult(); // always returns 0 if none
    }

    @Override
    public PostComment findById(Long commentId) {
        // الأفضل والأسهل
        PostComment c = entity.find(PostComment.class, commentId);
        if (c == null || Boolean.TRUE.equals(c.getDeleted())) {
            throw new RuntimeException("Comment not found");
        }
        return c;
    }

    @Override
    public List<PostComment> findByPostId(Long postId) {
        TypedQuery<PostComment> q = entity.createQuery(
                "FROM PostComment c WHERE c.postId = :postId AND c.isDeleted = false ORDER BY c.createdAt DESC",
                PostComment.class
        );
        q.setParameter("postId", postId);
        return q.getResultList();
    }

    @Override
    @Transactional
    public PostComment addComment(PostComment comment) {
        comment.setId(null);
        // createdAt راح ينحط من @PrePersist إذا موجود، أو حطه هنا لو مش عامل
        entity.persist(comment);
        return comment;
    }

    @Override
    @Transactional
    public PostComment updateComment(Long commentId, PostComment updated) {
        PostComment existing = findById(commentId);
        existing.setContent(updated.getContent());
        existing.setUpdatedAt(Instant.now()); // أو خليه @PreUpdate
        return existing; // managed داخل transaction
    }

    @Override
    @Transactional
    public String deleteComment(Long commentId) {
        PostComment existing = findById(commentId);
        // Soft delete
        existing.setDeleted(true);
        existing.setUpdatedAt(Instant.now());
        return "The comment with the id - " + commentId + " is deleted";
    }
}

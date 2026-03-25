package com.jomap.backend.Services.Community.Posts.Likes;

import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.postLikes.PostLikes;
import com.jomap.backend.Entities.Posts.postLikes.PostLikesRepository;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LikesServices implements LikesService {

    private final PostLikesRepository postLikes;

    @PersistenceContext
    private EntityManager entity;

    public LikesServices(PostLikesRepository postLikes, EntityManager entity) {
        this.postLikes = postLikes;
        this.entity = entity;
    }

    @Override
    public Long countByPostId(Long postId) {
        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId",
                Long.class);
        query.setParameter("postId", postId);
        return query.getSingleResult();
    }

    @Override
    public ResponseEntity<?> existsByPostId(Long postId) {
        Optional<User> optionalUser = getCurrentUser();

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                Long.class);
        query.setParameter("postId", postId);
        query.setParameter("userId", currentUser.getId());

        boolean exists = query.getSingleResult() > 0;

        return ResponseEntity.ok(Map.of("liked", exists));
    }

    @Override
    @Transactional
    public ResponseEntity<?> addLike(Long postId) {

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

        User currentUser = optionalUser.get();

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                Long.class);
        query.setParameter("postId", postId);
        query.setParameter("userId", currentUser.getId());

        boolean alreadyLiked = query.getSingleResult() > 0;

        if (alreadyLiked) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Already liked"));
        }

        PostLikes like = new PostLikes();
        like.setPost(post);
        like.setUser(currentUser);

        entity.persist(like);

        return ResponseEntity.ok(like);
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteByPostId(Long postId) {

        Optional<User> optionalUser = getCurrentUser();

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated user not found"));
        }

        User currentUser = optionalUser.get();

        TypedQuery<PostLikes> q = entity.createQuery(
                "FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                PostLikes.class);
        q.setParameter("postId", postId);
        q.setParameter("userId", currentUser.getId());

        List<PostLikes> list = q.getResultList();

        if (list.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Like not found for this user on this post"));
        }

        entity.remove(list.get(0));

        return ResponseEntity.ok(
                Map.of("message", "Unliked successfully"));
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }

        String email = authentication.getName();

        TypedQuery<User> query = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class);
        query.setParameter("email", email);

        List<User> users = query.getResultList();

        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.get(0));
    }
}
package com.start.demo.Services.Community.Posts.Likes;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Entities.Posts.postLikes.PostLikesRepository;
import com.start.demo.Entities.Users.User;
import com.start.demo.Services.Auth.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikesServices implements LikesService {

    private final PostLikesRepository postLikes;
    private final CurrentUserService currentUserService;

    @PersistenceContext
    private EntityManager entity;

    public LikesServices(PostLikesRepository postLikes, EntityManager entity, CurrentUserService currentUserService) {
        this.postLikes = postLikes;
        this.entity = entity;
        this.currentUserService = currentUserService;
    }

    @Override
    public Long countByPostId(Long postId) {
        if (postId == null || postId <= 0) {
            return 0L;
        }

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId",
                Long.class
        );
        query.setParameter("postId", postId);
        return query.getSingleResult();
    }

    @Override
    public Boolean existsByPostId(Long postId) {
        User currentUser = getCurrentUser();
        if (postId == null || postId <= 0 || currentUser == null) {
            return false;
        }

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                Long.class
        );
        query.setParameter("postId", postId);
        query.setParameter("userId", currentUser.getId());

        return query.getSingleResult() > 0;
    }

    @Override
    @Transactional
    public PostLikes addLike(Long postId) {
        if (postId == null || postId <= 0) {
            return null;
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            return null;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null || existsByPostId(postId)) {
            return null;
        }

        PostLikes like = new PostLikes();
        like.setPost(post);
        like.setUser(currentUser);

        entity.persist(like);
        return like;
    }

    @Override
    @Transactional
    public String deleteByPostId(Long postId) {
        User currentUser = getCurrentUser();
        if (postId == null || postId <= 0 || currentUser == null) {
            return null;
        }

        TypedQuery<PostLikes> q = entity.createQuery(
                "FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                PostLikes.class
        );
        q.setParameter("postId", postId);
        q.setParameter("userId", currentUser.getId());

        List<PostLikes> list = q.getResultList();
        if (list.isEmpty()) {
            return null;
        }

        entity.remove(list.get(0));
        return "Unliked successfully";
    }

    private User getCurrentUser() {
        try {
            return currentUserService.getCurrentUser();
        } catch (Exception ignored) {
            return null;
        }
    }
}

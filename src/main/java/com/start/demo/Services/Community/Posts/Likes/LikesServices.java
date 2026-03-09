package com.start.demo.Services.Community.Posts.Likes;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Entities.Posts.postLikes.PostLikesRepository;
import com.start.demo.Entities.Users.User;
import com.start.demo.Exciptions.DuplicateResourceException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
                Long.class
        );
        query.setParameter("postId", postId);
        return query.getSingleResult();
    }

    @Override
    public Boolean existsByPostId(Long postId) {
        User currentUser = getCurrentUser();

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

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User currentUser = getCurrentUser();

        if (existsByPostId(postId)) {
            throw new DuplicateResourceException("Already liked");
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

        TypedQuery<PostLikes> q = entity.createQuery(
                "FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                PostLikes.class
        );
        q.setParameter("postId", postId);
        q.setParameter("userId", currentUser.getId());

        List<PostLikes> list = q.getResultList();

        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Like not found for this user on post id: " + postId);
        }

        entity.remove(list.get(0));

        return "Unliked successfully";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
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

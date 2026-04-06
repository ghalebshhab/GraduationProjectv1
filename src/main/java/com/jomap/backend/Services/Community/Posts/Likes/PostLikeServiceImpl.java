package com.jomap.backend.Services.Community.Posts.Likes;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Likes.PostLikeResponse;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.postLikes.PostLikes;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entity;

    @Override
    @Transactional
    public ApiResponse<Long> countByPostId(Long postId) {
        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId",
                Long.class
        );
        query.setParameter("postId", postId);

        return ApiResponse.success("Likes count fetched successfully", query.getSingleResult());
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Boolean>> existsByPostId(String emailFromToken, Long postId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                Long.class
        );
        query.setParameter("postId", postId);
        query.setParameter("userId", currentUser.getId());

        boolean exists = query.getSingleResult() > 0;

        return ApiResponse.success("Like status fetched successfully", Map.of("liked", exists));
    }

    @Override
    @Transactional
    public ApiResponse<PostLikeResponse> addLike(String emailFromToken, Long postId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(l) FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                Long.class
        );
        query.setParameter("postId", postId);
        query.setParameter("userId", currentUser.getId());

        boolean alreadyLiked = query.getSingleResult() > 0;
        if (alreadyLiked) {
            return ApiResponse.error("Already liked");
        }

        PostLikes like = new PostLikes();
        like.setPost(post);
        like.setUser(currentUser);

        entity.persist(like);
        entity.flush();

        return ApiResponse.success("Post liked successfully", toResponse(like));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteByPostId(String emailFromToken, Long postId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        TypedQuery<PostLikes> q = entity.createQuery(
                "FROM PostLikes l WHERE l.post.id = :postId AND l.user.id = :userId",
                PostLikes.class
        );
        q.setParameter("postId", postId);
        q.setParameter("userId", currentUser.getId());

        List<PostLikes> list = q.getResultList();

        if (list.isEmpty()) {
            return ApiResponse.error("Like not found for this user on this post");
        }

        entity.remove(list.get(0));
        entity.flush();

        return ApiResponse.success("Post unliked successfully", "Post unliked successfully");
    }

    private PostLikeResponse toResponse(PostLikes like) {
        return new PostLikeResponse(
                like.getId(),
                like.getPost() != null ? like.getPost().getId() : null,
                like.getUser() != null ? like.getUser().getId() : null,
                like.getUser() != null ? like.getUser().getUsername() : null,
                like.getCreatedAt()
        );
    }
}
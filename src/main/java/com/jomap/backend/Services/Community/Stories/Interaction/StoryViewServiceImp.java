package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.StoryView;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StoryViewServiceImp implements StoryViewService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public ApiResponse<Long> countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(v) FROM StoryView v WHERE v.story.id = :storyId",
                Long.class
        );
        q.setParameter("storyId", storyId);

        return ApiResponse.success("Story views count fetched successfully", q.getSingleResult());
    }

    @Override
    public ApiResponse<Boolean> existsByStoryId(Long storyId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(v) FROM StoryView v WHERE v.story.id = :storyId AND v.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        return ApiResponse.success("Story view existence checked successfully", q.getSingleResult() > 0);
    }

    @Override
    @Transactional
    public ApiResponse<String> addView(Long storyId) {
        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            return ApiResponse.error("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(v) FROM StoryView v WHERE v.story.id = :storyId AND v.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        if (q.getSingleResult() > 0) {
            return ApiResponse.error("Already viewed");
        }

        StoryView view = new StoryView();
        view.setStory(story);
        view.setUser(currentUser);
        entity.persist(view);

        return ApiResponse.success("Story viewed successfully", "Story viewed successfully");
    }

    private User getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        String email = authentication.getName();

        TypedQuery<User> q = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        q.setParameter("email", email);

        List<User> users = q.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }
}
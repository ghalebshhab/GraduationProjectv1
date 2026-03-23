package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.StoryView;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Exciptions.DuplicateResourceException;
import com.jomap.backend.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoryViewServiceImp implements StoryViewService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public Long countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(v) FROM StoryView v WHERE v.story.id = :storyId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        return q.getSingleResult();
    }

    @Override
    public Boolean existsByStoryId(Long storyId) {
        User currentUser = getCurrentUser();

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(v) FROM StoryView v WHERE v.story.id = :storyId AND v.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        return q.getSingleResult() > 0;
    }

    @Override
    @Transactional
    public StoryView addView(Long storyId) {

        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();

        if (existsByStoryId(storyId)) {
            throw new DuplicateResourceException("Already viewed");
        }

        StoryView view = new StoryView();
        view.setStory(story);
        view.setUser(currentUser);

        entity.persist(view);

        return view;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        String email = authentication.getName();

        TypedQuery<User> q = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        q.setParameter("email", email);

        List<User> users = q.getResultList();

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        return users.get(0);
    }
}
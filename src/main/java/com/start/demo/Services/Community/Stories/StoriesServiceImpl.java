package com.start.demo.Services.Community.Stories;

import com.start.demo.DTOs.Stories.CreateStoryRequest;
import com.start.demo.DTOs.Stories.StoryResponse;
import com.start.demo.Entities.Stories.Story;
import com.start.demo.Entities.Stories.Storyrepo;
import com.start.demo.Entities.Users.User;
import com.start.demo.Services.Auth.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class StoriesServiceImpl implements StoryService {

    private final Storyrepo storyRepository;
    private final CurrentUserService currentUserService;

    @PersistenceContext
    private EntityManager entity;

    public StoriesServiceImpl(Storyrepo storyRepository, CurrentUserService currentUserService) {
        this.storyRepository = storyRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public StoryResponse create(CreateStoryRequest request) {
        User author = getCurrentUser();
        if (author == null || request == null || request.getMediaUrl() == null || request.getMediaUrl().isBlank()) {
            return null;
        }

        Story story = new Story();
        story.setAuthor(author);
        story.setMediaUrl(request.getMediaUrl());
        story.setCaption(request.getCaption());

        Integer hours = request.getExpiresInHours();
        if (hours != null) {
            if (hours <= 0) {
                return null;
            }
            story.setExpiresAt(Instant.now().plusSeconds((long) hours * 3600));
        }

        Story saved = storyRepository.save(story);
        return toResponse(saved);
    }

    @Override
    public List<StoryResponse> getActiveStories(int page, int size) {
        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s WHERE s.isDeleted = false AND s.expiresAt > :now ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setFirstResult(Math.max(page, 0) * Math.max(size, 1));
        q.setMaxResults(Math.max(size, 1));

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }
        return res;
    }

    @Override
    public List<StoryResponse> getActiveStoriesByUser(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }

        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s WHERE s.isDeleted = false AND s.expiresAt > :now AND s.author.id = :userId ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setParameter("userId", userId);
        q.setFirstResult(Math.max(page, 0) * Math.max(size, 1));
        q.setMaxResults(Math.max(size, 1));

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }
        return res;
    }

    @Override
    @Transactional
    public String deleteStory(Long storyId) {
        if (storyId == null || storyId <= 0) {
            return null;
        }

        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            return null;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null || story.getAuthor() == null || !story.getAuthor().getId().equals(currentUser.getId())) {
            return null;
        }

        story.setDeleted(true);
        return "Story deleted successfully";
    }

    private StoryResponse toResponse(Story s) {
        StoryResponse r = new StoryResponse();
        r.setId(s.getId());
        r.setMediaUrl(s.getMediaUrl());
        r.setCaption(s.getCaption());
        r.setCreatedAt(s.getCreatedAt());
        r.setExpiresAt(s.getExpiresAt());

        if (s.getAuthor() != null) {
            r.setAuthorId(s.getAuthor().getId());
            r.setAuthorEmail(s.getAuthor().getEmail());
        }

        return r;
    }

    private User getCurrentUser() {
        try {
            return currentUserService.getCurrentUser();
        } catch (Exception ignored) {
            return null;
        }
    }
}

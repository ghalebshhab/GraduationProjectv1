package com.jomap.backend.Services.Community.Stories;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.CreateStoryRequest;
import com.jomap.backend.DTOs.Stories.StoryResponse;
import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.Storyrepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class StoriesServiceImpl implements StoryService {

    private final Storyrepo storyRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entity;

    @Override
    @Transactional
    public ApiResponse<StoryResponse> create(CreateStoryRequest request) {
        User author = getCurrentUser();
        if (author == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        if (request.getMediaUrl() == null || request.getMediaUrl().isBlank()) {
            return ApiResponse.error("Media URL is required");
        }

        Story story = new Story();
        story.setAuthor(author);
        story.setMediaUrl(request.getMediaUrl().trim());
        story.setCaption(request.getCaption());

        Integer h = request.getExpiresInHours();
        if (h != null) {
            if (h <= 0) {
                return ApiResponse.error("expiresInHours must be greater than 0");
            }
            story.setExpiresAt(Instant.now().plusSeconds((long) h * 3600));
        }

        Story saved = storyRepository.save(story);
        return ApiResponse.success("Story created successfully", toResponse(saved));
    }

    @Override
    public ApiResponse<List<StoryResponse>> getActiveStories(int page, int size) {
        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s " +
                        "WHERE s.isDeleted = false AND s.expiresAt > :now " +
                        "ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }

        return ApiResponse.success("Active stories fetched successfully", res);
    }

    @Override
    public ApiResponse<List<StoryResponse>> getActiveStoriesByUser(Long userId, int page, int size) {
        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s " +
                        "WHERE s.isDeleted = false AND s.expiresAt > :now AND s.author.id = :userId " +
                        "ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setParameter("userId", userId);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }

        return ApiResponse.success("User active stories fetched successfully", res);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteStory(Long storyId) {
        Story story = entity.find(Story.class, storyId);

        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            return ApiResponse.error("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        if (story.getAuthor() == null || !story.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only delete your own story");
        }

        story.setDeleted(true);
        return ApiResponse.success("Story deleted successfully", "Story deleted successfully");
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
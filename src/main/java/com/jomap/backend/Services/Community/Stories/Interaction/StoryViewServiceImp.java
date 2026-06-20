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

import com.jomap.backend.DTOs.Stories.Views.StoryViewerResponse;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.time.Duration;

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

        if (story.getAuthor() != null && story.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("Cannot view your own story");
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

    @Override
    public ApiResponse<List<StoryViewerResponse>> getViewersByStoryId(Long storyId) {
        TypedQuery<StoryView> q = entity.createQuery(
                "SELECT v FROM StoryView v JOIN FETCH v.user u LEFT JOIN FETCH u.profile p WHERE v.story.id = :storyId ORDER BY v.viewedAt DESC",
                StoryView.class
        );
        q.setParameter("storyId", storyId);
        List<StoryView> views = q.getResultList();

        List<StoryViewerResponse> responseList = new ArrayList<>();
        for (StoryView v : views) {
            User u = v.getUser();
            StoryViewerResponse res = new StoryViewerResponse();
            res.setUserId(u.getId());
            res.setUsername(u.getUsername());
            res.setProfileImageUrl(u.getProfileImageUrl());
            if (u.getProfile() != null) {
                res.setFirstName(u.getProfile().getFirstName());
                res.setLastName(u.getProfile().getLastName());
            } else {
                res.setFirstName("");
                res.setLastName("");
            }
            res.setViewedAt(formatRelativeTime(v.getViewedAt()));
            responseList.add(res);
        }

        return ApiResponse.success("تم جلب المشاهدين بنجاح", responseList);
    }

    private String formatRelativeTime(Instant instant) {
        if (instant == null) return "";
        long diffSeconds = Duration.between(instant, Instant.now()).getSeconds();
        if (diffSeconds < 0) {
            diffSeconds = 0;
        }
        if (diffSeconds < 60) {
            return "منذ ثوانٍ";
        }
        long diffMinutes = diffSeconds / 60;
        if (diffMinutes < 60) {
            if (diffMinutes == 1) {
                return "منذ دقيقة";
            } else if (diffMinutes == 2) {
                return "منذ دقيقتين";
            } else if (diffMinutes <= 10) {
                return "منذ " + diffMinutes + " دقائق";
            } else {
                return "منذ " + diffMinutes + " دقيقة";
            }
        }
        long diffHours = diffMinutes / 60;
        if (diffHours < 24) {
            if (diffHours == 1) {
                return "منذ ساعة";
            } else if (diffHours == 2) {
                return "منذ ساعتين";
            } else if (diffHours <= 10) {
                return "منذ " + diffHours + " ساعات";
            } else {
                return "منذ " + diffHours + " ساعة";
            }
        }
        long diffDays = diffHours / 24;
        if (diffDays == 1) {
            return "منذ يوم";
        } else if (diffDays == 2) {
            return "منذ يومين";
        } else if (diffDays <= 10) {
            return "منذ " + diffDays + " أيام";
        } else {
            return "منذ " + diffDays + " يوم";
        }
    }
}
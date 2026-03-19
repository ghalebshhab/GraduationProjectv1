package com.start.demo.Entities.Users;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postComments.PostComment;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Entities.Stories.Story;
import com.start.demo.Entities.Stories.StoryLike;
import com.start.demo.Entities.Stories.StoryView;
import com.start.demo.Entities.Stories.StoryLike;
import com.start.demo.Entities.Stories.StoryView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Setter
    @Getter
    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Setter
    @Getter
    @Column(nullable = false, name = "password_hash", length = 255)
    private String passwordHash;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Getter
    @Setter
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Getter
    @Setter
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @Setter
    @Getter
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // =========================
    // Relationships
    // =========================

    // One user can create many posts
    @Setter
    @Getter
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Post> posts = new ArrayList<>();

    // One user can write many post comments
    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PostComment> comments = new ArrayList<>();

    // One user can make many post likes
    @Setter
    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PostLikes> likes = new ArrayList<>();

    // One user can create many stories
    @Setter
    @Getter
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Story> stories = new ArrayList<>();

    // One user can view many stories
    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<StoryView> storyViews = new ArrayList<>();

    // One user can react to many stories
    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<StoryLike> storyReactions = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public User() {
    }

    public User(String email, String username, String passwordHash, Role role, Boolean isActive, Instant createdAt, Instant updatedAt) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }

    public void setStoryViews(List<StoryView> storyViews) {
        this.storyViews = storyViews;
    }

    public void setStoryReactions(List<StoryLike> storyReactions) {
        this.storyReactions = storyReactions;
    }
}
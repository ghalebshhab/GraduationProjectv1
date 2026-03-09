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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(nullable = false, name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // =========================
    // Relationships
    // =========================

    // One user can create many posts
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Post> posts = new ArrayList<>();

    // One user can write many post comments
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PostComment> comments = new ArrayList<>();

    // One user can make many post likes
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PostLikes> likes = new ArrayList<>();

    // One user can create many stories
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Story> stories = new ArrayList<>();

    // One user can view many stories
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<StoryView> storyViews = new ArrayList<>();

    // One user can react to many stories
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }

    public List<PostLikes> getLikes() {
        return likes;
    }

    public void setLikes(List<PostLikes> likes) {
        this.likes = likes;
    }

    public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }

    public List<StoryView> getStoryViews() {
        return storyViews;
    }

    public void setStoryViews(List<StoryView> storyViews) {
        this.storyViews = storyViews;
    }

    public List<StoryLike> getStoryReactions() {
        return storyReactions;
    }

    public void setStoryReactions(List<StoryLike> storyReactions) {
        this.storyReactions = storyReactions;
    }
}
package com.jomap.backend.Entities.Posts;

import com.jomap.backend.Entities.Posts.postLikes.PostLikes;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    public enum PostType { COMMUNITY, EVENT, OFFER }

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many posts belong to one user
    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // One post can have many comments
    @Setter
    @Getter
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<PostComment> comments = new ArrayList<>();

    // One post can have many likes
    @Setter
    @Getter
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<PostLikes> likes = new ArrayList<>();

    @Setter
    @Getter
    @Column(nullable = false, length = 2000)
    private String content;

    @Setter
    @Getter
    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type = PostType.COMMUNITY;

    @Setter
    @Getter
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Setter
    @Getter
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @Setter
    @Getter
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Post() {}

    public Post(User author, String content, String mediaUrl, PostType type) {
        this.author = author;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.type = type;
    }

    }
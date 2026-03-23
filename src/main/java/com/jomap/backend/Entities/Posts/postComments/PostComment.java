package com.jomap.backend.Entities.Posts.postComments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "post_comments")
@AllArgsConstructor

public class PostComment {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many comments belong to one post
    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    // Many comments belong to one user
    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Setter
    @Getter
    @Column(nullable = false, length = 1000)
    private String content;

    @Setter
    @Getter
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
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

    public PostComment() {}



}
package com.jomap.backend.Entities.Posts;

import com.jomap.backend.Entities.Posts.postLikes.PostLikes;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@Data
public class Post {

    public enum PostType { COMMUNITY, EVENT, OFFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<PostComment> comments = new ArrayList<>();


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<PostLikes> likes = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String content;


    @Column(name = "media_url", length = 500)
    private String mediaUrl;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type = PostType.COMMUNITY;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;


    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "category")
    private String category;
    @Column(name = "lat")
    private Double latitude;
    @Column(name = "lon")
    private Double longitude;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }



    public Post(User author, String content, String mediaUrl, PostType type) {
        this.author = author;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.type = type;
    }

    }
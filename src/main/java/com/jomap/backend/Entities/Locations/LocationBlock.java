package com.jomap.backend.Entities.Locations;

import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "location_blocks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "location_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class LocationBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationList blockedLocation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public LocationBlock(User blocker, LocationList blockedLocation, LocalDateTime blockedAt) {
        this.blocker = blocker;
        this.blockedLocation = blockedLocation;
        this.createdAt = blockedAt;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

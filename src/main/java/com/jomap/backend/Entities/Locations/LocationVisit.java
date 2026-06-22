package com.jomap.backend.Entities.Locations;

import java.time.LocalDateTime;

import com.jomap.backend.Entities.Users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * يسجّل كل زيارة لصفحة منشأة من قِبَل مستخدم محدد.
 * يُستخدم لمنع احتساب زيارات متكررة خلال نافذة زمنية (cooldown = 1 ساعة).
 * الزوار غير المسجّلين (guests) تُسجَّل زياراتهم دون تتبع userId.
 */
@Entity
@Table(name = "location_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationList location;

    /**
     * المستخدم الزائر — يكون null للزوار غير المسجّلين.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private User visitor;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;
}

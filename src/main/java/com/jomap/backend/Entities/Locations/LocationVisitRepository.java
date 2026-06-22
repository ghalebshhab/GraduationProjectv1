package com.jomap.backend.Entities.Locations;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationVisitRepository extends JpaRepository<LocationVisit, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
            FROM LocationVisit v
            WHERE v.location.id = :locationId
              AND v.visitor.id = :visitorId
              AND v.visitedAt >= :since
            """)
    boolean existsRecentVisitByUser(
            @Param("locationId") Long locationId,
            @Param("visitorId") Long visitorId,
            @Param("since") LocalDateTime since
    );
}

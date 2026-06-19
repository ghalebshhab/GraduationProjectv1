package com.jomap.backend.Entities.Locations;

import com.jomap.backend.Entities.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationBlockRepository extends JpaRepository<LocationBlock, Long> {

    boolean existsByBlockerAndBlockedLocation(User blocker, LocationList blockedLocation);

    void deleteByBlockerAndBlockedLocation(User blocker, LocationList blockedLocation);

    java.util.List<LocationBlock> findByBlocker(User blocker);

    java.util.Optional<LocationBlock> findByBlockerAndBlockedLocation(User blocker, LocationList blockedLocation);

    @Query("SELECT l.blockedLocation.id FROM LocationBlock l WHERE l.blocker.id = :userId")
    List<Long> findBlockedLocationIdsByBlockerId(@Param("userId") Long userId);
}


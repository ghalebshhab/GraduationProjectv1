package com.jomap.backend.Entities.Locations;

import com.jomap.backend.Entities.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationBlockRepository extends JpaRepository<LocationBlock, Long> {

    boolean existsByBlockerAndBlockedLocation(User blocker, LocationList blockedLocation);

    void deleteByBlockerAndBlockedLocation(User blocker, LocationList blockedLocation);
}

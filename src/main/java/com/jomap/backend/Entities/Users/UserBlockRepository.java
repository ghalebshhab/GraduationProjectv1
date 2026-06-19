package com.jomap.backend.Entities.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

    List<UserBlock> findByBlocker(User blocker);

    void deleteByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT u.blocked.id FROM UserBlock u WHERE u.blocker.id = :userId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("userId") Long userId);

    @Query("SELECT u.blocker.id FROM UserBlock u WHERE u.blocked.id = :userId")
    List<Long> findBlockedUserIdsByBlockedId(@Param("userId") Long userId);
}


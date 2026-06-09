package com.jomap.backend.Entities.Activities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByActivityId(Long activityId);
    boolean existsByActivityIdAndUserId(Long activityId, Long userId);
    java.util.Optional<Registration> findByActivityIdAndUserId(Long activityId, Long userId);
}

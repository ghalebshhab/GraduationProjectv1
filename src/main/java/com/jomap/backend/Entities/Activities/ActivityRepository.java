package com.jomap.backend.Entities.Activities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStatus(ActivityStatus status);

    List<Activity> findByStatusAndGovernorateId(ActivityStatus status, Long governorateId);

    List<Activity> findByStatusAndDateGreaterThanEqual(ActivityStatus status, LocalDate date);

    List<Activity> findByCreatedById(Long userId);

    List<Activity> findByStatusAndActivityLocationContainingIgnoreCase(ActivityStatus status, String location);
}
package com.jomap.backend.Entities.Activities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStatus(ActivityStatus status);

    List<Activity> findByStatusAndGovernorateId(ActivityStatus status, Long governorateId);

    List<Activity> findByStatusAndDateGreaterThanEqual(ActivityStatus status, LocalDate date);

    List<Activity> findByCreatedById(Long userId);

    List<Activity> findByStatusAndActivityLocationContainingIgnoreCase(ActivityStatus status, String location);

    @Query(value = "SELECT * FROM activities WHERE governorate_id = :govId AND status = 'APPROVED' ORDER BY date ASC LIMIT 5", nativeQuery = true)
    List<Activity> findTop5Activities(@Param("govId") Long govId);
}
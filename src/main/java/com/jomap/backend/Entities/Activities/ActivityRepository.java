package com.jomap.backend.Entities.Activities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStatus(ActivityStatus status);

    List<Activity> findByStatusAndGovernorateId(ActivityStatus status, Long governorateId);

    List<Activity> findByCreatedById(Long userId);

    List<Activity> findByLocationId(Long locationId);

    List<Activity> findByStatusAndActivityLocationContainingIgnoreCase(ActivityStatus status, String location);

    @Query(value = "SELECT DISTINCT a.* FROM activities a " +
                   "JOIN activity_schedules s ON a.id = s.activity_id " +
                   "WHERE a.governorate_id = :govId AND a.status = 'APPROVED' " +
                   "ORDER BY s.date ASC LIMIT 5", nativeQuery = true)
    List<Activity> findTop5Activities(@Param("govId") Long govId);

    List<Activity> findTop10ByStatusOrderByIdDesc(ActivityStatus status);

    List<Activity> findTop10ByStatusInOrderByIdDesc(List<ActivityStatus> statuses);

    List<Activity> findByStatusInOrderByIdDesc(List<ActivityStatus> statuses);
}
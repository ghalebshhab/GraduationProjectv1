package com.jomap.backend.Entities.Events;

import com.jomap.backend.Entities.Events.Event;
import com.jomap.backend.Entities.Events.EventStatus;
import com.jomap.backend.Entities.Gove.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndGovernorate(EventStatus status, String governorate);

    List<Event> findByStatusAndDateGreaterThanEqual(EventStatus status, LocalDate date);

    List<Event> findByCreatedById(Long userId);

    List<Event> findByStatusAndGovernorateId(EventStatus status, Long governorateId);

    @Query(value = "SELECT * FROM events WHERE governorate_id = :govId AND status = 'APPROVED' ORDER BY date ASC LIMIT 5", nativeQuery = true)
    List<Event> findTop5Events(@Param("govId") Long govId);
}
package com.jomap.backend.Entities.Events;

import com.jomap.backend.Entities.Events.Event;
import com.jomap.backend.Entities.Events.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndGovernorate(EventStatus status, String governorate);

    List<Event> findByStatusAndDateGreaterThanEqual(EventStatus status, LocalDate date);

    List<Event> findByCreatedById(Long userId);
}
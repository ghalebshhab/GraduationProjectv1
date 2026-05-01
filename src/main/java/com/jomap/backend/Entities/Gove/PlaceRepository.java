package com.jomap.backend.Entities.Gove;

import com.jomap.backend.Entities.Gove.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByGovernorateId(Long governorateId);
}
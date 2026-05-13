package com.jomap.backend.Entities.Gove;

import com.jomap.backend.Entities.Gove.Place;
import com.jomap.backend.Entities.Locations.LocationList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByGovernorateId(Long governorateId);

    @Query(value = "SELECT * FROM location_lists WHERE governorate_id = :govId ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
    List<LocationList> findRandomSuggestions(@Param("govId") Long govId);


    @Query(value = "SELECT * FROM location_lists WHERE governorate_id = :govId AND category = 'TOURISM' LIMIT 5", nativeQuery = true)
    List<LocationList> findTopHistoricalPlaces(@Param("govId") Long govId);
}
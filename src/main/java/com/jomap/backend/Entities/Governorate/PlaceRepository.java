package com.jomap.backend.Entities.Governorate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    List<Place> findByGovernorateId(Long governorateId);

    // سحب مقترحات عشوائية ثابتة تخص المحافظة
    @Query(value = "SELECT * FROM places WHERE governorate_id = :govId ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
    List<Place> findRandomSuggestions(@Param("govId") Long govId);

    // سحب الأماكن التاريخية بناءً على القيمة العالمية الجديدة HISTORICAL
    @Query(value = "SELECT * FROM places WHERE governorate_id = :govId AND category = 'HISTORICAL' LIMIT 5", nativeQuery = true)
    List<Place> findTopHistoricalPlaces(@Param("govId") Long govId);
}
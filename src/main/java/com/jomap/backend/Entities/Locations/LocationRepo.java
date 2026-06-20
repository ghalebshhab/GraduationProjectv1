package com.jomap.backend.Entities.Locations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepo extends JpaRepository<LocationList, Long> {

    @Query("SELECT l FROM LocationList l WHERE l.status = com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED")
    List<LocationList> findByActiveTrueAndApprovedTrue();

    @Query("SELECT l FROM LocationList l WHERE l.governorate.id = :governorateId AND l.status = com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED")
    List<LocationList> findByGovernorateIdAndActiveTrueAndApprovedTrue(Long governorateId);

    @Query("SELECT l FROM LocationList l WHERE l.category = :category AND l.status = com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED")
    List<LocationList> findByCategoryAndActiveTrueAndApprovedTrue(LocationCategory category);

    @Query("SELECT l FROM LocationList l WHERE l.governorate.id = :governorateId AND l.category = :category AND l.status = com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED")
    List<LocationList> findByGovernorateIdAndCategoryAndActiveTrueAndApprovedTrue(
            Long governorateId,
            LocationCategory category
    );

    Optional<LocationList> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);

    @Query("SELECT l FROM LocationList l WHERE l.status = com.jomap.backend.Entities.Locations.LocationStatus.PENDING")
    List<LocationList> findByApprovedFalseAndActiveTrue();

    List<LocationList> findByStatus(LocationStatus status);

    List<LocationList> findByStatusOrderByIdDesc(LocationStatus status);

    long countByStatus(LocationStatus status);

    long countByActiveTrue();
    long countByActiveFalse();

    @Query("SELECT COUNT(l) FROM LocationList l WHERE l.status = com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED")
    long countByApprovedTrueAndActiveTrue();

    @Query("SELECT COUNT(l) FROM LocationList l WHERE l.status = com.jomap.backend.Entities.Locations.LocationStatus.PENDING")
    long countByApprovedFalseAndActiveTrue();

    List<LocationList> findByStatusAndDeletedAtBefore(LocationStatus status, LocalDateTime thresholdTime);

    Optional<LocationList> findByOwnerIdAndStatusNot(Long ownerId, LocationStatus status);

    List<LocationList> findTop10ByActiveTrueAndStatusOrderByIdDesc(LocationStatus status);
    org.springframework.data.domain.Page<LocationList> findByActiveTrueAndStatusOrderByIdDesc(LocationStatus status, org.springframework.data.domain.Pageable pageable);
}

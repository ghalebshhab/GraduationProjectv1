package com.jomap.backend.Entities.Locations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepo extends JpaRepository<LocationList, Long> {

    List<LocationList> findByActiveTrueAndApprovedTrue();

    List<LocationList> findByGovernorateIdAndActiveTrueAndApprovedTrue(Long governorateId);

    List<LocationList> findByCategoryAndActiveTrueAndApprovedTrue(LocationCategory category);

    List<LocationList> findByGovernorateIdAndCategoryAndActiveTrueAndApprovedTrue(
            Long governorateId,
            LocationCategory category
    );

    Optional<LocationList> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);

    List<LocationList> findByApprovedFalseAndActiveTrue();

    long countByActiveTrue();
    long countByActiveFalse();
    long countByApprovedTrueAndActiveTrue();
    long countByApprovedFalseAndActiveTrue();

    List<LocationList> findByStatusAndDeletedAtBefore(LocationStatus status, LocalDateTime thresholdTime);

    Optional<LocationList> findByOwnerIdAndStatusNot(Long ownerId, LocationStatus status);

    List<LocationList> findTop10ByActiveTrueAndStatusOrderByIdDesc(LocationStatus status);
    org.springframework.data.domain.Page<LocationList> findByActiveTrueAndStatusOrderByIdDesc(LocationStatus status, org.springframework.data.domain.Pageable pageable);
}
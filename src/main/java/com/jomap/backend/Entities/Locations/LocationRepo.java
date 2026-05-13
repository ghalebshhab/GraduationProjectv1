package com.jomap.backend.Entities.Locations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
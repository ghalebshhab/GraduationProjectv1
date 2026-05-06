package com.jomap.backend.Entities.Places;
import com.jomap.backend.Entities.Gove.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocationListRepo extends JpaRepository<LocationList, Long> {

    List<LocationList> findByActiveTrueAndApprovedTrue();

    List<LocationList> findByGovernorateIgnoreCaseAndActiveTrueAndApprovedTrue(String governorate);

    List<LocationList> findByCategoryAndActiveTrueAndApprovedTrue(PlaceCategory category);

    List<LocationList> findByGovernorateIgnoreCaseAndCategoryAndActiveTrueAndApprovedTrue(
            String governorate,
            PlaceCategory category
    );

    Optional<LocationList> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);
    List<LocationList> findByApprovedFalseAndActiveTrue();

    long countByActiveTrue();

    long countByActiveFalse();

    long countByApprovedTrueAndActiveTrue();

    long countByApprovedFalseAndActiveTrue();

}
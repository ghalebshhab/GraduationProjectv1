package com.jomap.backend.Entities.Places;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByActiveTrueAndApprovedTrue();

    List<Place> findByGovernorateIgnoreCaseAndActiveTrueAndApprovedTrue(String governorate);

    List<Place> findByCategoryAndActiveTrueAndApprovedTrue(PlaceCategory category);

    List<Place> findByGovernorateIgnoreCaseAndCategoryAndActiveTrueAndApprovedTrue(
            String governorate,
            PlaceCategory category
    );

    Optional<Place> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);
    List<Place> findByApprovedFalseAndActiveTrue();

    long countByActiveTrue();

    long countByActiveFalse();

    long countByApprovedTrueAndActiveTrue();

    long countByApprovedFalseAndActiveTrue();
}
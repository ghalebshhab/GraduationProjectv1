package com.jomap.backend.Entities.Governorate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GovernorateImageRepository extends JpaRepository<GovernorateImage, Long> {
    List<GovernorateImage> findByGovernorateId(Long governorateId);
}
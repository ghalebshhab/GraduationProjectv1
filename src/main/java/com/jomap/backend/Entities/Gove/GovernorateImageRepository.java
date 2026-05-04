package com.jomap.backend.Entities.Gove;

import com.jomap.backend.Entities.Gove.GovernorateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovernorateImageRepository extends JpaRepository<GovernorateImage, Long> {
    List<GovernorateImage> findByGovernorateId(Long governorateId);
}

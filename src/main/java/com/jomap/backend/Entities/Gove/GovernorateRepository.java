package com.jomap.backend.Entities.Gove;

import com.jomap.backend.Entities.Gove.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GovernorateRepository extends JpaRepository<Governorate, Long> {
}
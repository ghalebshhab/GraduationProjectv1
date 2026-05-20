package com.jomap.backend.Entities.Governorate;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GovernorateRepository extends JpaRepository<Governorate, Long> {
    Optional<Governorate> findByName(@NotBlank(message = "المحافظة مطلوبة") String governorate);
}
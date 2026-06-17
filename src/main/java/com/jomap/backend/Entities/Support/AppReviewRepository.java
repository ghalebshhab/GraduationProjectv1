package com.jomap.backend.Entities.Support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppReviewRepository extends JpaRepository<AppReview, Long> {
    boolean existsByUserId(Long userId);
}

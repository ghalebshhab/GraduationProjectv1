package com.jomap.backend.Entities.Offers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepo extends JpaRepository<Offer, Long> {

    List<Offer> findByStatus(OfferStatus status);

    List<Offer> findByStatusAndGovernorateId(OfferStatus status, Long governorateId);

    List<Offer> findByCreatedById(Long userId);

    List<Offer> findByLocationId(Long locationId);

    // جلب آخر 10 عروض مقبولة نزلت بالسيستم للـ Community Feed
    List<Offer> findTop10ByStatusOrderByIdDesc(OfferStatus status);

    @Query("SELECT DISTINCT o FROM Offer o JOIN o.products p WHERE o.governorate.id = :govId AND o.status = 'APPROVED' ORDER BY o.id DESC")
    List<Offer> findTop5OffersByGovernorate(@Param("govId") Long govId);

    List<Offer> findAllByOrderByIdDesc();
}
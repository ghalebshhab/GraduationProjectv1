package com.jomap.backend.Services.Offers;

import com.jomap.backend.Entities.Offers.Offer;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Offers.OfferStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class OfferStatusScheduler {

    private final OfferRepo offerRepo;

    public OfferStatusScheduler(OfferRepo offerRepo) {
        this.offerRepo = offerRepo;
    }

    // Runs every 10 minutes (600,000 milliseconds)
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void updateOfferStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Scheduled to Active
        List<Offer> scheduledOffers = offerRepo.findByStatus(OfferStatus.SCHEDULED);
        for (Offer offer : scheduledOffers) {
            try {
                LocalDate startDate = LocalDate.parse(offer.getStartDate());
                LocalTime startTime = LocalTime.parse(offer.getStartTime());
                LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

                if (!now.isBefore(startDateTime)) {
                    offer.setStatus(OfferStatus.ACTIVE);
                    offerRepo.save(offer);
                }
            } catch (Exception e) {
                System.err.println("Error parsing start date/time for offer " + offer.getId() + ": " + e.getMessage());
            }
        }

        // 2. Active to Expired
        List<Offer> activeOffers = offerRepo.findByStatus(OfferStatus.ACTIVE);
        for (Offer offer : activeOffers) {
            try {
                LocalDate endDate = LocalDate.parse(offer.getEndDate());
                LocalTime endTime = LocalTime.parse(offer.getEndTime());
                LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

                if (now.isAfter(endDateTime)) {
                    offer.setStatus(OfferStatus.EXPIRED);
                    offerRepo.save(offer);
                }
            } catch (Exception e) {
                System.err.println("Error parsing end date/time for offer " + offer.getId() + ": " + e.getMessage());
            }
        }
    }
}

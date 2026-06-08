package com.jomap.backend.Entities.Offers;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "offer_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "offer")
public class OfferProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    @JsonBackReference("offer-products")
    private Offer offer;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double priceBefore;

    @Column(nullable = false)
    private Double priceAfter;
}

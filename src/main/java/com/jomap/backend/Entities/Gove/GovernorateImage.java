package com.jomap.backend.Entities.Gove;

import com.jomap.backend.Entities.Gove.Governorate;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "governorate_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "governorate")
public class GovernorateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    @JsonBackReference
    private Governorate governorate;
}
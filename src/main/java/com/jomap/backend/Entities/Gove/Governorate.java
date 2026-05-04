package com.jomap.backend.Entities.Gove;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "governorates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "images") // لمنع مشاكل التكرار في الذاكرة
public class Governorate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "governorate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<GovernorateImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "governorate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("governorate-places")
    private List<Place> places = new ArrayList<>();
}
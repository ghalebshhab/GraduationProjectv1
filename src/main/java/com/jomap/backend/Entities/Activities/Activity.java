package com.jomap.backend.Entities.Activities;

import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activities") 
@Getter
@Setter
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;

    @Column(name = "total_actual_days", nullable = false)
    private Integer totalActualDays = 1;

    @jakarta.persistence.OneToMany(mappedBy = "activity", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ActivitySchedule> schedules = new java.util.ArrayList<>();

    @Column(name = "activity_location")
    private String activityLocation; 
    
    @Column(nullable = true)
    private Double price ;

    @Column(nullable = true)
    private Integer maxCapacity;

    @Column(nullable = true)
    private Integer attendeesCount ; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    private String imageUrl;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status = ActivityStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
}

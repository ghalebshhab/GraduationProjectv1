package com.jomap.backend.Entities.Activities;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activity_registrations")
@Getter
@Setter
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "registration_time", nullable = false)
    private LocalTime registrationTime;

    @PrePersist
    public void prePersist() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
        if (this.registrationTime == null) {
            this.registrationTime = LocalTime.now();
        }
        if (this.status == null) {
            this.status = RegistrationStatus.PENDING;
        }
    }
}

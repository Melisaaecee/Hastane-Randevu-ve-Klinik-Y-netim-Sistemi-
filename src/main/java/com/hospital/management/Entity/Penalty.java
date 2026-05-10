package com.hospital.management.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIgnore
    private Appointment appointment;

    @Column(nullable = false)
    private LocalDateTime penaltyStartDate;

    @Column(nullable = false)
    private LocalDateTime penaltyEndDate;

    @Column(nullable = false)
    private boolean active = true;
}
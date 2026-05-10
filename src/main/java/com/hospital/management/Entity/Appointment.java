package com.hospital.management.Entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @OneToOne
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private Slot slot;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

}
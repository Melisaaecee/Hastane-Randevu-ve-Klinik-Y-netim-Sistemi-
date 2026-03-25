package com.hospital.management.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctorId") 
    private Long id;

    @Column(name = "specialization")
    private String specialization;

  
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId") 
    private User user;

    @ManyToOne
    @JoinColumn(name = "clinicId")
    private Clinic clinic;
}
package com.hospital.management.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "penalties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic; // Cezanın hangi bölüm için olduğunu belirtir

    @Column(nullable = false)
    private LocalDateTime penaltyStartDate; // Ceza başlangıcı (Randevuya gidilmeyen an)

    @Column(nullable = false)
    private LocalDateTime penaltyEndDate; // Ceza bitişi (Başlangıç + 7 Gün)

    @Column(nullable = false)
    private boolean active = true; // Ceza hala geçerli mi?
}
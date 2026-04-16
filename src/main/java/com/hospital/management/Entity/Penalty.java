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
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment; // 🔥 cezanın kaynağı

    @Column(nullable = false)
    private LocalDateTime penaltyStartDate; // Ceza başlangıcı (Randevuya gidilmeyen an)

    @Column(nullable = false)
    private LocalDateTime penaltyEndDate; // Ceza bitişi (Başlangıç + 7 Gün)

    @Column(nullable = false)
    private boolean active = true; // Ceza hala geçerli mi?
}
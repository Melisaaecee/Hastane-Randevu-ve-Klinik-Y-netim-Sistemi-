package com.hospital.management.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false)
    private LocalDate birthDate;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private BloodType bloodType;

@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false, unique = true)
private User user;


@Transient
    public int getAge() {
        if (this.birthDate != null) {
            return Period.between(this.birthDate, LocalDate.now()).getYears();
        }
        return 0;
    }

 
@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
private List<Penalty> penalties;
}
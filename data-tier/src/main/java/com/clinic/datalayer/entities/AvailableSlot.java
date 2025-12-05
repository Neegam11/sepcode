package com.clinic.datalayer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "available_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slotId;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String status;

    @OneToOne(mappedBy = "slot")
    private Appointment appointment;
}

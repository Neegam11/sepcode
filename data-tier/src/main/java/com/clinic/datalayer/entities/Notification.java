package com.clinic.datalayer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String recipientType;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    private String channel;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.clinic.datalayer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;


@Entity
@Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Notification> sentNotifications;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Appointment> managedAppointments;
}

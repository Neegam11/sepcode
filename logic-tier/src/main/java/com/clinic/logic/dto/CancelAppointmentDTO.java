package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentDTO {
    private Long appointmentId;
    private String cancelledBy; // PATIENT, DOCTOR
    private String reason;
}


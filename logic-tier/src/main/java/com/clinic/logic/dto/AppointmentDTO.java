package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private String type;
    private Long staffId;
    private String cancellationReason;
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;
}


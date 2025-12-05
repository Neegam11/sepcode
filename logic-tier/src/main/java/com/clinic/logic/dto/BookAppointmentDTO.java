package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentDTO {
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private String type;
}


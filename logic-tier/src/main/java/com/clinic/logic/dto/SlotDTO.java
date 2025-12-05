package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {
    private Long slotId;
    private Long doctorId;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private Long appointmentId;
    private String doctorName;
    private String doctorSpecialization;
}


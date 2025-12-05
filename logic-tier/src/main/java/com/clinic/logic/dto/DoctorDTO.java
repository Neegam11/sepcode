package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private Long doctorId;
    private String name;
    private String specialization;
    private String email;
}


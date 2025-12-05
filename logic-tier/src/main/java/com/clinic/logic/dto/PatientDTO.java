package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long patientId;
    private String name;
    private String email;
    private String phone;
}


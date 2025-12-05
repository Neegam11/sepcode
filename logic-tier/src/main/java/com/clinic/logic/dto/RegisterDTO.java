package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String userType; // PATIENT, DOCTOR, STAFF
    private String specialization; // For doctors
    private String role; // For staff
}


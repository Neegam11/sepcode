package com.clinic.logic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for registration requests.
 * Note: Only patients can self-register. Doctors and Staff are pre-configured.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String userType; // PATIENT, DOCTOR, STAFF (only PATIENT allowed for self-registration)

    private String specialization; // For doctors (not used in self-registration)

    private String role; // For staff (not used in self-registration)
}

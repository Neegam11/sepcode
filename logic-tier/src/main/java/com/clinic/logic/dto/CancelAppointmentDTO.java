package com.clinic.logic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentDTO {

    private Long appointmentId;

    @NotBlank(message = "Cancelled by is required")
    @Pattern(regexp = "^(PATIENT|DOCTOR|STAFF)$", message = "Cancelled by must be PATIENT, DOCTOR, or STAFF")
    private String cancelledBy;

    private String reason;
}

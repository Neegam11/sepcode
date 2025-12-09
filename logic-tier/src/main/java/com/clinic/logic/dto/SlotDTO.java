package com.clinic.logic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for available slot information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {

    private Long slotId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Start time must be in HH:mm format")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "End time must be in HH:mm format")
    private String endTime;

    private String status;

    private Long appointmentId;

    private String doctorName;

    private String doctorSpecialization;
}

package com.clinic.logic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for notification information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long notificationId;

    private Long appointmentId;

    private Long staffId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Recipient type is required")
    @Pattern(regexp = "^(PATIENT|DOCTOR|STAFF)$", message = "Recipient type must be PATIENT, DOCTOR, or STAFF")
    private String recipientType;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Notification type is required")
    private String type;

    private String status;

    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "^(EMAIL|SMS|PUSH|IN_APP)$", message = "Channel must be EMAIL, SMS, PUSH, or IN_APP")
    private String channel;

    private String createdAt;
}

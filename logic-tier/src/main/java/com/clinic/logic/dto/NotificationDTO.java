package com.clinic.logic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private Long appointmentId;
    private Long staffId;
    private Long recipientId;
    private String recipientType;
    private String message;
    private String type;
    private String status;
    private String channel;
    private String createdAt;
}


package com.clinic.logic.controller;

import com.clinic.grpc.NotificationMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private DataTierClient dataTierClient;

    @GetMapping("/user/{userType}/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUserNotifications(
            @PathVariable String userType,
            @PathVariable Long userId) {
        List<NotificationMessage> notifications = dataTierClient.getUserNotifications(userId, userType.toUpperCase());
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long id) {
        return dataTierClient.markNotificationAsRead(id)
                .map(n -> ResponseEntity.ok(ApiResponse.success("Marked as read", convertToDTO(n))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationDTO>> sendNotification(@RequestBody NotificationDTO dto) {
        return dataTierClient.sendNotification(
                dto.getAppointmentId() != null ? dto.getAppointmentId() : 0L,
                dto.getStaffId() != null ? dto.getStaffId() : 0L,
                dto.getRecipientId(),
                dto.getRecipientType(),
                dto.getMessage(),
                dto.getType(),
                dto.getChannel()
        ).map(n -> ResponseEntity.ok(ApiResponse.success("Notification sent", convertToDTO(n))))
         .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to send notification")));
    }

    private NotificationDTO convertToDTO(NotificationMessage message) {
        return new NotificationDTO(
                message.getNotificationId(),
                message.getAppointmentId(),
                message.getStaffId(),
                message.getRecipientId(),
                message.getRecipientType(),
                message.getMessage(),
                message.getType(),
                message.getStatus(),
                message.getChannel(),
                message.getCreatedAt()
        );
    }
}


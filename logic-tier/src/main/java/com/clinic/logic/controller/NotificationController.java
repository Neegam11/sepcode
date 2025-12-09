package com.clinic.logic.controller;

import com.clinic.grpc.NotificationMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private DataTierClient dataTierClient;


    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String recipientType) {

        if (recipientId == null || recipientType == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Both recipientId and recipientType are required"));
        }

        List<NotificationMessage> notifications = dataTierClient.getUserNotifications(
                recipientId, recipientType.toUpperCase());
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDTO>> updateNotification(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if ("READ".equalsIgnoreCase(status)) {
            return dataTierClient.markNotificationAsRead(id)
                    .map(n -> ResponseEntity.ok(ApiResponse.success("Notification updated", convertToDTO(n))))
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status value"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(@RequestBody NotificationDTO dto) {
        return dataTierClient.sendNotification(
                        dto.getAppointmentId() != null ? dto.getAppointmentId() : 0L,
                        dto.getStaffId() != null ? dto.getStaffId() : 0L,
                        dto.getRecipientId(),
                        dto.getRecipientType(),
                        dto.getMessage(),
                        dto.getType(),
                        dto.getChannel()
                ).map(n -> ResponseEntity.ok(ApiResponse.success("Notification created", convertToDTO(n))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create notification")));
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

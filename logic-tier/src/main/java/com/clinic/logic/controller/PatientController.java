package com.clinic.logic.controller;

import com.clinic.grpc.AppointmentMessage;
import com.clinic.grpc.NotificationMessage;
import com.clinic.grpc.PatientMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private DataTierClient dataTierClient;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getAllPatients() {
        List<PatientMessage> patients = dataTierClient.getAllPatients();
        List<PatientDTO> dtos = patients.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        return dataTierClient.getPatientById(id)
                .map(patient -> ResponseEntity.ok(ApiResponse.success(convertToDTO(patient))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getPatientAppointments(@PathVariable Long id) {
        List<AppointmentMessage> appointments = dataTierClient.getPatientAppointments(id);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getPatientNotifications(@PathVariable Long id) {
        List<NotificationMessage> notifications = dataTierClient.getUserNotifications(id, "PATIENT");
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertNotificationToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    private PatientDTO convertToDTO(PatientMessage message) {
        return new PatientDTO(
                message.getPatientId(),
                message.getName(),
                message.getEmail(),
                message.getPhone()
        );
    }

    private AppointmentDTO convertAppointmentToDTO(AppointmentMessage message) {
        return new AppointmentDTO(
                message.getAppointmentId(),
                message.getPatientId(),
                message.getDoctorId(),
                message.getSlotId(),
                message.getDate(),
                message.getStartTime(),
                message.getEndTime(),
                message.getStatus(),
                message.getType(),
                message.getStaffId(),
                message.getCancellationReason(),
                message.getPatientName(),
                message.getDoctorName(),
                message.getDoctorSpecialization()
        );
    }

    private NotificationDTO convertNotificationToDTO(NotificationMessage message) {
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


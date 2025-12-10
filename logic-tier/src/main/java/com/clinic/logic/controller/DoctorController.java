package com.clinic.logic.controller;

import com.clinic.grpc.AppointmentMessage;
import com.clinic.grpc.AvailableSlotMessage;
import com.clinic.grpc.DoctorMessage;
import com.clinic.grpc.NotificationMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DataTierClient dataTierClient;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getAllDoctors() {
        List<DoctorMessage> doctors = dataTierClient.getAllDoctors();
        List<DoctorDTO> dtos = doctors.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorById(@PathVariable Long id) {
        return dataTierClient.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(ApiResponse.success(convertToDTO(doctor))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<DoctorMessage> doctors = dataTierClient.getDoctorsBySpecialization(specialization);
        List<DoctorDTO> dtos = doctors.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDoctorAppointments(@PathVariable Long id) {
        List<AppointmentMessage> appointments = dataTierClient.getDoctorAppointments(id);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDoctorDailySchedule(
            @PathVariable Long id,
            @RequestParam(required = false) String date) {
        String scheduleDate = date != null ? date : LocalDate.now().toString();
        List<AppointmentMessage> appointments = dataTierClient.getDoctorDailySchedule(id, scheduleDate);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getDoctorSlots(@PathVariable Long id) {
        List<AvailableSlotMessage> slots = dataTierClient.getDoctorSlots(id);
        List<SlotDTO> dtos = slots.stream()
                .map(this::convertSlotToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getDoctorNotifications(@PathVariable Long id) {
        List<NotificationMessage> notifications = dataTierClient.getUserNotifications(id, "DOCTOR");
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertNotificationToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PatchMapping("/{doctorId}/appointments/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId,
            @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        if (status != null) {
            return dataTierClient.updateAppointmentStatus(appointmentId, status, 0)
                    .map(appointment -> ResponseEntity.ok(ApiResponse.success("Appointment updated", convertAppointmentToDTO(appointment))))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to update appointment")));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("No valid update fields provided"));
    }

    @DeleteMapping("/{doctorId}/appointments/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Cancelled by doctor") : "Cancelled by doctor";
        return dataTierClient.cancelAppointment(appointmentId, "DOCTOR", reason)
                .map(appointment -> ResponseEntity.ok(ApiResponse.success("Appointment cancelled", convertAppointmentToDTO(appointment))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to cancel appointment")));
    }

    private DoctorDTO convertToDTO(DoctorMessage message) {
        return new DoctorDTO(
                message.getDoctorId(),
                message.getName(),
                message.getSpecialization(),
                message.getEmail()
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

    private SlotDTO convertSlotToDTO(AvailableSlotMessage message) {
        return new SlotDTO(
                message.getSlotId(),
                message.getDoctorId(),
                message.getDate(),
                message.getStartTime(),
                message.getEndTime(),
                message.getStatus(),
                message.getAppointmentId(),
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


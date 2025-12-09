package com.clinic.logic.controller;

import com.clinic.grpc.AppointmentMessage;
import com.clinic.grpc.NotificationMessage;
import com.clinic.grpc.PatientMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Patient resource.
 *
 * RESTful Endpoints:
 * - GET    /api/patients                    - Get all patients
 * - GET    /api/patients/{id}               - Get patient by ID
 * - GET    /api/patients/{id}/appointments  - Get patient's appointments
 * - GET    /api/patients/{id}/notifications - Get patient's notifications
 */
@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    private final DataTierClient dataTierClient;

    // Constructor injection (best practice)
    public PatientController(DataTierClient dataTierClient) {
        this.dataTierClient = dataTierClient;
    }

    /**
     * GET /api/patients
     * Retrieve all patients.
     *
     * @return 200 OK with list of patients
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getAllPatients() {
        List<PatientMessage> patients = dataTierClient.getAllPatients();
        List<PatientDTO> dtos = patients.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * GET /api/patients/{id}
     * Retrieve a specific patient by ID.
     *
     * @param id Patient ID
     * @return 200 OK with patient, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long id) {
        return dataTierClient.getPatientById(id)
                .map(patient -> ResponseEntity.ok(ApiResponse.success(convertToDTO(patient))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patient not found with ID: " + id)));
    }

    /**
     * GET /api/patients/{id}/appointments
     * Retrieve all appointments for a specific patient.
     *
     * @param id Patient ID
     * @return 200 OK with list of appointments
     */
    @GetMapping("/{id}/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getPatientAppointments(@PathVariable Long id) {
        List<AppointmentMessage> appointments = dataTierClient.getPatientAppointments(id);
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * GET /api/patients/{id}/notifications
     * Retrieve all notifications for a specific patient.
     *
     * @param id Patient ID
     * @return 200 OK with list of notifications
     */
    @GetMapping("/{id}/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getPatientNotifications(@PathVariable Long id) {
        List<NotificationMessage> notifications = dataTierClient.getUserNotifications(id, "PATIENT");
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertNotificationToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    // ==================== DTO Converters ====================

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

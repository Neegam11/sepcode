package com.clinic.logic.controller;

import com.clinic.grpc.AppointmentMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private DataTierClient dataTierClient;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAllAppointments() {
        List<AppointmentMessage> appointments = dataTierClient.getAllAppointments();

        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(@PathVariable Long id) {
        Optional<AppointmentMessage> result = dataTierClient.getAppointmentById(id);

        if (result.isPresent()) {
            AppointmentDTO dto = convertToDTO(result.get());
            return ResponseEntity.ok(ApiResponse.success(dto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(@RequestBody BookAppointmentDTO dto) {
        Optional<AppointmentMessage> result = dataTierClient.bookAppointment(
                dto.getPatientId(),
                dto.getDoctorId(),
                dto.getSlotId(),
                dto.getType()
        );

        if (result.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", convertToDTO(result.get())));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to book appointment"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) CancelAppointmentDTO dto) {

        String cancelledBy = (dto != null) ? dto.getCancelledBy() : "PATIENT";
        String reason = (dto != null) ? dto.getReason() : "Cancelled by user";

        Optional<AppointmentMessage> result = dataTierClient.cancelAppointment(id, cancelledBy, reason);

        if (result.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", convertToDTO(result.get())));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to cancel appointment"));
        }
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {


        if (body.containsKey("status")) {
            String status = (String) body.get("status");
            Long staffId = extractLong(body, "staffId", 0L);

            Optional<AppointmentMessage> result = dataTierClient.updateAppointmentStatus(id, status, staffId);

            if (result.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Appointment updated", convertToDTO(result.get())));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update appointment"));
            }
        }


        if (body.containsKey("doctorId") || body.containsKey("slotId")) {
            Long newDoctorId = extractLong(body, "doctorId", 0L);
            Long newSlotId = extractLong(body, "slotId", 0L);

            Optional<AppointmentMessage> result = dataTierClient.reassignAppointment(id, newDoctorId, newSlotId);

            if (result.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Appointment reassigned", convertToDTO(result.get())));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to reassign appointment"));
            }
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("No valid update fields provided"));
    }

    private Long extractLong(Map<String, Object> map, String key, Long defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    private AppointmentDTO convertToDTO(AppointmentMessage message) {
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
}
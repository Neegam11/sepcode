package com.clinic.logic.controller;

import com.clinic.grpc.AppointmentMessage;
import com.clinic.grpc.AvailableSlotMessage;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return dataTierClient.getAppointmentById(id)
                .map(appointment -> ResponseEntity.ok(ApiResponse.success(convertToDTO(appointment))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<AppointmentDTO>> bookAppointment(@RequestBody BookAppointmentDTO dto) {
        return dataTierClient.bookAppointment(
                        dto.getPatientId(),
                        dto.getDoctorId(),
                        dto.getSlotId(),
                        dto.getType()
                ).map(appointment -> ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", convertToDTO(appointment))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to book appointment")));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody CancelAppointmentDTO dto) {
        return dataTierClient.cancelAppointment(id, dto.getCancelledBy(), dto.getReason())
                .map(appointment -> ResponseEntity.ok(ApiResponse.success("Appointment cancelled", convertToDTO(appointment))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to cancel appointment")));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Long staffId = body.get("staffId") != null ? ((Number) body.get("staffId")).longValue() : 0L;

        return dataTierClient.updateAppointmentStatus(id, status, staffId)
                .map(appointment -> ResponseEntity.ok(ApiResponse.success("Status updated", convertToDTO(appointment))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to update status")));
    }

    @PatchMapping("/{id}/reassign")
    public ResponseEntity<ApiResponse<AppointmentDTO>> reassignAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long newDoctorId = body.get("doctorId") != null ? ((Number) body.get("doctorId")).longValue() : 0L;
        Long newSlotId = body.get("slotId") != null ? ((Number) body.get("slotId")).longValue() : 0L;

        return dataTierClient.reassignAppointment(id, newDoctorId, newSlotId)
                .map(appointment -> ResponseEntity.ok(ApiResponse.success("Appointment reassigned", convertToDTO(appointment))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to reassign appointment")));
    }

    @GetMapping("/slots/available")
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getAvailableSlots(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date) {
        List<AvailableSlotMessage> slots = dataTierClient.getAvailableSlots(doctorId, date);
        List<SlotDTO> dtos = slots.stream()
                .map(this::convertSlotToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
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
}
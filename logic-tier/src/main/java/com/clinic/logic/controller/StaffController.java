package com.clinic.logic.controller;

import com.clinic.grpc.*;
import com.clinic.logic.dto.*;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private DataTierClient dataTierClient;


    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAllAppointments() {
        List<AppointmentMessage> appointments = dataTierClient.getAllAppointments();
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }


    @PatchMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        if (body.containsKey("doctorId") || body.containsKey("slotId")) {
            Long newDoctorId = body.get("doctorId") != null ? ((Number) body.get("doctorId")).longValue() : 0L;
            Long newSlotId = body.get("slotId") != null ? ((Number) body.get("slotId")).longValue() : 0L;
            return dataTierClient.reassignAppointment(id, newDoctorId, newSlotId)
                    .map(a -> ResponseEntity.ok(ApiResponse.success("Appointment updated", convertAppointmentToDTO(a))))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to update appointment")));
        }

        if (body.containsKey("status")) {
            String status = (String) body.get("status");
            Long staffId = body.get("staffId") != null ? ((Number) body.get("staffId")).longValue() : 0L;
            return dataTierClient.updateAppointmentStatus(id, status, staffId)
                    .map(a -> ResponseEntity.ok(ApiResponse.success("Appointment updated", convertAppointmentToDTO(a))))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to update appointment")));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("No valid update fields provided"));
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        String reason = (body != null && body.get("reason") != null)
                ? (String) body.get("reason")
                : "Cancelled by staff";

        return dataTierClient.cancelAppointment(id, "STAFF", reason)
                .map(a -> ResponseEntity.ok(ApiResponse.success("Appointment cancelled", convertAppointmentToDTO(a))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to cancel appointment")));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(@PathVariable Long id) {
        return dataTierClient.getAppointmentById(id)
                .map(a -> ResponseEntity.ok(ApiResponse.success(convertAppointmentToDTO(a))))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getAvailableSlots(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date) {
        List<AvailableSlotMessage> slots = dataTierClient.getAvailableSlots(doctorId, date);
        List<SlotDTO> dtos = slots.stream()
                .map(this::convertSlotToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<SlotDTO>> createSlot(@RequestBody SlotDTO slotDTO) {
        return dataTierClient.createSlot(
                        slotDTO.getDoctorId(),
                        slotDTO.getDate(),
                        slotDTO.getStartTime(),
                        slotDTO.getEndTime()
                ).map(s -> ResponseEntity.ok(ApiResponse.success("Slot created", convertSlotToDTO(s))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create slot")));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSlot(@PathVariable Long id) {
        StatusResponse response = dataTierClient.deleteSlot(id);
        if (response.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }


    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(@RequestBody NotificationDTO dto) {
        return dataTierClient.sendNotification(
                        dto.getAppointmentId() != null ? dto.getAppointmentId() : 0L,
                        dto.getStaffId() != null ? dto.getStaffId() : 0L,
                        dto.getRecipientId(),
                        dto.getRecipientType(),
                        dto.getMessage(),
                        dto.getType(),
                        dto.getChannel()
                ).map(n -> ResponseEntity.ok(ApiResponse.success("Notification created", convertNotificationToDTO(n))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create notification")));
    }


    @GetMapping("/reports/schedule")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateScheduleReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long doctorId) {

        ScheduleReportResponse report = dataTierClient.generateScheduleReport(startDate, endDate, doctorId);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalAppointments", report.getTotalAppointments());
        reportData.put("completedAppointments", report.getCompletedAppointments());
        reportData.put("cancelledAppointments", report.getCancelledAppointments());
        reportData.put("missedAppointments", report.getMissedAppointments());

        List<Map<String, Object>> doctorSummaries = report.getDoctorSummariesList().stream()
                .map(summary -> {
                    Map<String, Object> ds = new HashMap<>();
                    ds.put("doctorId", summary.getDoctorId());
                    ds.put("doctorName", summary.getDoctorName());
                    ds.put("totalAppointments", summary.getTotalAppointments());
                    ds.put("availableSlots", summary.getAvailableSlots());
                    return ds;
                }).toList();
        reportData.put("doctorSummaries", doctorSummaries);

        return ResponseEntity.ok(ApiResponse.success(reportData));
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


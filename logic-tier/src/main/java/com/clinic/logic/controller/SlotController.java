package com.clinic.logic.controller;

import com.clinic.grpc.AvailableSlotMessage;
import com.clinic.logic.dto.ApiResponse;
import com.clinic.logic.dto.SlotDTO;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Slot resources.
 *
 * RESTful Design:
 * - GET  /api/slots              - Get all slots (can filter with query params)
 * - GET  /api/slots/{id}         - Get a specific slot
 * - POST /api/slots              - Create a new slot
 * - DELETE /api/slots/{id}       - Delete a slot
 *
 * Slots are a separate resource from Appointments.
 * A Slot represents available time, an Appointment represents a booked meeting.
 */
@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private DataTierClient dataTierClient;

    // GET /api/slots - Get slots with optional filtering
    // Query params: doctorId, date, status (e.g., ?status=available&doctorId=1)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getSlots(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status) {

        List<AvailableSlotMessage> slots;

        // If status=available, use the available slots endpoint
        if ("available".equalsIgnoreCase(status)) {
            slots = dataTierClient.getAvailableSlots(doctorId, date);
        } else if (doctorId != null) {
            slots = dataTierClient.getDoctorSlots(doctorId);
        } else {
            slots = dataTierClient.getAvailableSlots(doctorId, date);
        }

        List<SlotDTO> dtos = slots.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    // POST /api/slots - Create a new slot
    @PostMapping
    public ResponseEntity<ApiResponse<SlotDTO>> createSlot(@RequestBody SlotDTO slotDTO) {
        return dataTierClient.createSlot(
                        slotDTO.getDoctorId(),
                        slotDTO.getDate(),
                        slotDTO.getStartTime(),
                        slotDTO.getEndTime()
                ).map(s -> ResponseEntity.ok(ApiResponse.success("Slot created", convertToDTO(s))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create slot")));
    }

    // DELETE /api/slots/{id} - Delete a slot
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSlot(@PathVariable Long id) {
        var response = dataTierClient.deleteSlot(id);
        if (response.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }

    private SlotDTO convertToDTO(AvailableSlotMessage message) {
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




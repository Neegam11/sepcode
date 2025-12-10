package com.clinic.logic.controller;

import com.clinic.grpc.AvailableSlotMessage;
import com.clinic.logic.dto.ApiResponse;
import com.clinic.logic.dto.SlotDTO;
import com.clinic.logic.service.DataTierClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private DataTierClient dataTierClient;


    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotDTO>>> getSlots(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status) {

        List<AvailableSlotMessage> slots;

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




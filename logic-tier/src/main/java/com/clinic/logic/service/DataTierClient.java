package com.clinic.logic.service;

import com.clinic.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * gRPC Client for communicating with the Data Tier.
 */
@Service
public class DataTierClient {

    private static final Logger logger = LoggerFactory.getLogger(DataTierClient.class);

    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;
    private final PatientServiceGrpc.PatientServiceBlockingStub patientStub;
    private final DoctorServiceGrpc.DoctorServiceBlockingStub doctorStub;
    private final StaffServiceGrpc.StaffServiceBlockingStub staffStub;
    private final AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentStub;
    private final AvailableSlotServiceGrpc.AvailableSlotServiceBlockingStub slotStub;
    private final NotificationServiceGrpc.NotificationServiceBlockingStub notificationStub;
    private final ReportServiceGrpc.ReportServiceBlockingStub reportStub;

    public DataTierClient(ManagedChannel channel) {
        this.authStub = AuthServiceGrpc.newBlockingStub(channel);
        this.patientStub = PatientServiceGrpc.newBlockingStub(channel);
        this.doctorStub = DoctorServiceGrpc.newBlockingStub(channel);
        this.staffStub = StaffServiceGrpc.newBlockingStub(channel);
        this.appointmentStub = AppointmentServiceGrpc.newBlockingStub(channel);
        this.slotStub = AvailableSlotServiceGrpc.newBlockingStub(channel);
        this.notificationStub = NotificationServiceGrpc.newBlockingStub(channel);
        this.reportStub = ReportServiceGrpc.newBlockingStub(channel);
    }

    // ==================== Authentication ====================

    public LoginResponse login(String email, String password, String userType) {
        try {
            LoginRequest request = LoginRequest.newBuilder()
                    .setEmail(email)
                    .setPassword(password)
                    .setUserType(userType)
                    .build();
            return authStub.login(request);
        } catch (StatusRuntimeException e) {
            logger.error("Login failed: {}", e.getStatus());
            return LoginResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Login failed: " + e.getStatus().getDescription())
                    .build();
        }
    }

    public StatusResponse register(String name, String email, String phone, String password, 
                                   String userType, String specialization, String role) {
        try {
            RegisterRequest request = RegisterRequest.newBuilder()
                    .setName(name)
                    .setEmail(email)
                    .setPhone(phone != null ? phone : "")
                    .setPassword(password)
                    .setUserType(userType)
                    .setSpecialization(specialization != null ? specialization : "")
                    .setRole(role != null ? role : "")
                    .build();
            return authStub.register(request);
        } catch (StatusRuntimeException e) {
            logger.error("Registration failed: {}", e.getStatus());
            return StatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Registration failed: " + e.getStatus().getDescription())
                    .build();
        }
    }

    // ==================== Patient ====================

    public List<PatientMessage> getAllPatients() {
        try {
            return patientStub.getAllPatients(Empty.newBuilder().build()).getPatientsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get patients: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public Optional<PatientMessage> getPatientById(long id) {
        try {
            return Optional.of(patientStub.getPatientById(IdRequest.newBuilder().setId(id).build()));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get patient: {}", e.getStatus());
            return Optional.empty();
        }
    }

    // ==================== Doctor ====================

    public List<DoctorMessage> getAllDoctors() {
        try {
            return doctorStub.getAllDoctors(Empty.newBuilder().build()).getDoctorsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctors: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public Optional<DoctorMessage> getDoctorById(long id) {
        try {
            return Optional.of(doctorStub.getDoctorById(IdRequest.newBuilder().setId(id).build()));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctor: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public List<DoctorMessage> getDoctorsBySpecialization(String specialization) {
        try {
            return doctorStub.getDoctorsBySpecialization(
                    DoctorMessage.newBuilder().setSpecialization(specialization).build()
            ).getDoctorsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctors by specialization: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    // ==================== Appointments ====================

    public Optional<AppointmentMessage> bookAppointment(long patientId, long doctorId, long slotId, String type) {
        try {
            BookAppointmentRequest request = BookAppointmentRequest.newBuilder()
                    .setPatientId(patientId)
                    .setDoctorId(doctorId)
                    .setSlotId(slotId)
                    .setType(type != null ? type : "CONSULTATION")
                    .build();
            return Optional.of(appointmentStub.bookAppointment(request));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to book appointment: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public List<AppointmentMessage> getPatientAppointments(long patientId) {
        try {
            return appointmentStub.getPatientAppointments(
                    IdRequest.newBuilder().setId(patientId).build()
            ).getAppointmentsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get patient appointments: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public List<AppointmentMessage> getDoctorAppointments(long doctorId) {
        try {
            return appointmentStub.getDoctorAppointments(
                    IdRequest.newBuilder().setId(doctorId).build()
            ).getAppointmentsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctor appointments: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public List<AppointmentMessage> getDoctorDailySchedule(long doctorId, String date) {
        try {
            AppointmentFilterRequest request = AppointmentFilterRequest.newBuilder()
                    .setDoctorId(doctorId)
                    .setDate(date)
                    .build();
            return appointmentStub.getDoctorDailySchedule(request).getAppointmentsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctor schedule: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public List<AppointmentMessage> getAllAppointments() {
        try {
            return appointmentStub.getAllAppointments(Empty.newBuilder().build()).getAppointmentsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get all appointments: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public Optional<AppointmentMessage> getAppointmentById(long id) {
        try {
            return Optional.of(appointmentStub.getAppointmentById(
                    IdRequest.newBuilder().setId(id).build()));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get appointment: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public Optional<AppointmentMessage> cancelAppointment(long appointmentId, String cancelledBy, String reason) {
        try {
            CancelAppointmentRequest request = CancelAppointmentRequest.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setCancelledBy(cancelledBy)
                    .setReason(reason != null ? reason : "")
                    .build();
            return Optional.of(appointmentStub.cancelAppointment(request));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to cancel appointment: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public Optional<AppointmentMessage> updateAppointmentStatus(long appointmentId, String status, long staffId) {
        try {
            UpdateAppointmentStatusRequest request = UpdateAppointmentStatusRequest.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setStatus(status)
                    .setStaffId(staffId)
                    .build();
            return Optional.of(appointmentStub.updateAppointmentStatus(request));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to update appointment status: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public Optional<AppointmentMessage> reassignAppointment(long appointmentId, long newDoctorId, long newSlotId) {
        try {
            AppointmentMessage.Builder builder = AppointmentMessage.newBuilder()
                    .setAppointmentId(appointmentId);
            if (newDoctorId > 0) builder.setDoctorId(newDoctorId);
            if (newSlotId > 0) builder.setSlotId(newSlotId);
            return Optional.of(appointmentStub.reassignAppointment(builder.build()));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to reassign appointment: {}", e.getStatus());
            return Optional.empty();
        }
    }

    // ==================== Available Slots ====================

    public List<AvailableSlotMessage> getAvailableSlots(Long doctorId, String date) {
        try {
            SlotFilterRequest.Builder builder = SlotFilterRequest.newBuilder();
            if (doctorId != null && doctorId > 0) builder.setDoctorId(doctorId);
            if (date != null && !date.isEmpty()) builder.setDate(date);
            return slotStub.getAvailableSlots(builder.build()).getSlotsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get available slots: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public List<AvailableSlotMessage> getDoctorSlots(long doctorId) {
        try {
            return slotStub.getDoctorSlots(IdRequest.newBuilder().setId(doctorId).build()).getSlotsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get doctor slots: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public Optional<AvailableSlotMessage> createSlot(long doctorId, String date, String startTime, String endTime) {
        try {
            AvailableSlotMessage request = AvailableSlotMessage.newBuilder()
                    .setDoctorId(doctorId)
                    .setDate(date)
                    .setStartTime(startTime)
                    .setEndTime(endTime)
                    .build();
            return Optional.of(slotStub.createSlot(request));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to create slot: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public StatusResponse deleteSlot(long slotId) {
        try {
            return slotStub.deleteSlot(IdRequest.newBuilder().setId(slotId).build());
        } catch (StatusRuntimeException e) {
            logger.error("Failed to delete slot: {}", e.getStatus());
            return StatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to delete slot")
                    .build();
        }
    }

    // ==================== Notifications ====================

    public Optional<NotificationMessage> sendNotification(long appointmentId, long staffId, 
            long recipientId, String recipientType, String message, String type, String channel) {
        try {
            SendNotificationRequest request = SendNotificationRequest.newBuilder()
                    .setAppointmentId(appointmentId)
                    .setStaffId(staffId)
                    .setRecipientId(recipientId)
                    .setRecipientType(recipientType)
                    .setMessage(message)
                    .setType(type != null ? type : "MANUAL")
                    .setChannel(channel != null ? channel : "EMAIL")
                    .build();
            return Optional.of(notificationStub.sendNotification(request));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to send notification: {}", e.getStatus());
            return Optional.empty();
        }
    }

    public List<NotificationMessage> getUserNotifications(long recipientId, String recipientType) {
        try {
            NotificationFilterRequest request = NotificationFilterRequest.newBuilder()
                    .setRecipientId(recipientId)
                    .setRecipientType(recipientType)
                    .build();
            return notificationStub.getUserNotifications(request).getNotificationsList();
        } catch (StatusRuntimeException e) {
            logger.error("Failed to get notifications: {}", e.getStatus());
            return Collections.emptyList();
        }
    }

    public Optional<NotificationMessage> markNotificationAsRead(long notificationId) {
        try {
            return Optional.of(notificationStub.markNotificationAsRead(
                    IdRequest.newBuilder().setId(notificationId).build()));
        } catch (StatusRuntimeException e) {
            logger.error("Failed to mark notification as read: {}", e.getStatus());
            return Optional.empty();
        }
    }

    // ==================== Reports ====================

    public ScheduleReportResponse generateScheduleReport(String startDate, String endDate, Long doctorId) {
        try {
            ScheduleReportRequest.Builder builder = ScheduleReportRequest.newBuilder();
            if (startDate != null) builder.setStartDate(startDate);
            if (endDate != null) builder.setEndDate(endDate);
            if (doctorId != null && doctorId > 0) builder.setDoctorId(doctorId);
            return reportStub.generateScheduleReport(builder.build());
        } catch (StatusRuntimeException e) {
            logger.error("Failed to generate report: {}", e.getStatus());
            return ScheduleReportResponse.getDefaultInstance();
        }
    }
}


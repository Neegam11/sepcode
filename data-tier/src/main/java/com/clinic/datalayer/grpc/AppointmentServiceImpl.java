package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Appointment;
import com.clinic.datalayer.entities.AvailableSlot;
import com.clinic.datalayer.entities.Notification;
import com.clinic.datalayer.repositories.*;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@GRpcService
public class AppointmentServiceImpl extends AppointmentServiceGrpc.AppointmentServiceImplBase {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AvailableSlotRepository availableSlotRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void bookAppointment(BookAppointmentRequest request, StreamObserver<AppointmentMessage> responseObserver) {
        try {

            var patientOpt = patientRepository.findById(request.getPatientId());
            if (patientOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Patient not found").asRuntimeException());
                return;
            }

            var doctorOpt = doctorRepository.findById(request.getDoctorId());
            if (doctorOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Doctor not found").asRuntimeException());
                return;
            }

            var slotOpt = availableSlotRepository.findById(request.getSlotId());
            if (slotOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Slot not found").asRuntimeException());
                return;
            }

            AvailableSlot slot = slotOpt.get();
            if (!"AVAILABLE".equals(slot.getStatus())) {
                responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION.withDescription("Slot is not available").asRuntimeException());
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setPatient(patientOpt.get());
            appointment.setDoctor(doctorOpt.get());
            appointment.setSlot(slot);
            appointment.setDate(slot.getDate());
            appointment.setStartTime(slot.getStartTime());
            appointment.setEndTime(slot.getEndTime());
            appointment.setStatus("SCHEDULED");
            appointment.setType(request.getType().isEmpty() ? "CONSULTATION" : request.getType());

            Appointment savedAppointment = appointmentRepository.save(appointment);

            slot.setStatus("BOOKED");
            availableSlotRepository.save(slot);

            createNotification(savedAppointment, "BOOKING_CONFIRMATION", 
                "Your appointment has been booked for " + savedAppointment.getDate() + " at " + savedAppointment.getStartTime());

            responseObserver.onNext(convertToMessage(savedAppointment));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Failed to book appointment: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAppointmentById(IdRequest request, StreamObserver<AppointmentMessage> responseObserver) {
        appointmentRepository.findById(request.getId())
            .ifPresentOrElse(
                appointment -> {
                    responseObserver.onNext(convertToMessage(appointment));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Appointment not found").asRuntimeException())
            );
    }

    @Override
    public void getAllAppointments(Empty request, StreamObserver<AppointmentListResponse> responseObserver) {
        var appointments = appointmentRepository.findAll();
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPatientAppointments(IdRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        var appointments = appointmentRepository.findByPatientPatientId(request.getId());
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorAppointments(IdRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        var appointments = appointmentRepository.findByDoctorDoctorId(request.getId());
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorDailySchedule(AppointmentFilterRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        LocalDate date = request.getDate().isEmpty() ? LocalDate.now() : LocalDate.parse(request.getDate());
        var appointments = appointmentRepository.findActiveDoctorAppointmentsByDate(request.getDoctorId(), date);
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAppointmentsByFilter(AppointmentFilterRequest request, StreamObserver<AppointmentListResponse> responseObserver) {
        List<Appointment> appointments;
        if (request.getPatientId() > 0) {
            appointments = appointmentRepository.findByPatientPatientId(request.getPatientId());
        } else if (request.getDoctorId() > 0) {
            appointments = appointmentRepository.findByDoctorDoctorId(request.getDoctorId());
        } else if (!request.getStatus().isEmpty()) {
            appointments = appointmentRepository.findByStatus(request.getStatus());
        } else {
            appointments = appointmentRepository.findAll();
        }
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelAppointment(CancelAppointmentRequest request, StreamObserver<AppointmentMessage> responseObserver) {
        appointmentRepository.findById(request.getAppointmentId())
            .ifPresentOrElse(
                appointment -> {
                    String newStatus = "CANCELLED_BY_" + request.getCancelledBy().toUpperCase();
                    appointment.setStatus(newStatus);
                    appointment.setCancellationReason(request.getReason());
                    
                    if (appointment.getSlot() != null) {
                        AvailableSlot slot = appointment.getSlot();
                        slot.setStatus("AVAILABLE");
                        availableSlotRepository.save(slot);
                    }
                    
                    Appointment savedAppointment = appointmentRepository.save(appointment);
                    createNotification(savedAppointment, "CANCELLATION", "Appointment cancelled. Reason: " + request.getReason());
                    
                    responseObserver.onNext(convertToMessage(savedAppointment));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Appointment not found").asRuntimeException())
            );
    }

    @Override
    public void updateAppointmentStatus(UpdateAppointmentStatusRequest request, StreamObserver<AppointmentMessage> responseObserver) {
        appointmentRepository.findById(request.getAppointmentId())
            .ifPresentOrElse(
                appointment -> {
                    appointment.setStatus(request.getStatus());
                    if (request.getStaffId() > 0) {
                        staffRepository.findById(request.getStaffId()).ifPresent(appointment::setStaff);
                    }
                    Appointment savedAppointment = appointmentRepository.save(appointment);
                    createNotification(savedAppointment, "UPDATE", "Appointment status updated to: " + request.getStatus());
                    responseObserver.onNext(convertToMessage(savedAppointment));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Appointment not found").asRuntimeException())
            );
    }

    @Override
    public void updateAppointment(AppointmentMessage request, StreamObserver<AppointmentMessage> responseObserver) {
        appointmentRepository.findById(request.getAppointmentId())
            .ifPresentOrElse(
                appointment -> {
                    if (!request.getStatus().isEmpty()) appointment.setStatus(request.getStatus());
                    if (!request.getType().isEmpty()) appointment.setType(request.getType());
                    Appointment savedAppointment = appointmentRepository.save(appointment);
                    responseObserver.onNext(convertToMessage(savedAppointment));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Appointment not found").asRuntimeException())
            );
    }

    @Override
    public void reassignAppointment(AppointmentMessage request, StreamObserver<AppointmentMessage> responseObserver) {
        appointmentRepository.findById(request.getAppointmentId())
            .ifPresentOrElse(
                appointment -> {
                    if (request.getDoctorId() > 0) {
                        doctorRepository.findById(request.getDoctorId()).ifPresent(appointment::setDoctor);
                    }
                    Appointment savedAppointment = appointmentRepository.save(appointment);
                    createNotification(savedAppointment, "UPDATE", "Appointment reassigned to Dr. " + savedAppointment.getDoctor().getName());
                    responseObserver.onNext(convertToMessage(savedAppointment));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Appointment not found").asRuntimeException())
            );
    }

    @Override
    public void getPendingCancellations(Empty request, StreamObserver<AppointmentListResponse> responseObserver) {
        var appointments = appointmentRepository.findPendingCancellations();
        AppointmentListResponse.Builder builder = AppointmentListResponse.newBuilder();
        appointments.forEach(a -> builder.addAppointments(convertToMessage(a)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAppointment(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (appointmentRepository.existsById(request.getId())) {
            appointmentRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(true).setMessage("Appointment deleted").build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(false).setMessage("Appointment not found").build());
        }
        responseObserver.onCompleted();
    }

    private void createNotification(Appointment appointment, String type, String message) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setRecipientId(appointment.getPatient().getPatientId());
        notification.setRecipientType("PATIENT");
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("PENDING");
        notification.setChannel("EMAIL");
        notificationRepository.save(notification);
    }

    private AppointmentMessage convertToMessage(Appointment appointment) {
        AppointmentMessage.Builder builder = AppointmentMessage.newBuilder()
            .setAppointmentId(appointment.getAppointmentId())
            .setDate(appointment.getDate().toString())
            .setStartTime(appointment.getStartTime().toString())
            .setEndTime(appointment.getEndTime().toString())
            .setStatus(appointment.getStatus() != null ? appointment.getStatus() : "")
            .setType(appointment.getType() != null ? appointment.getType() : "")
            .setCancellationReason(appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "");

        if (appointment.getPatient() != null) {
            builder.setPatientId(appointment.getPatient().getPatientId());
            builder.setPatientName(appointment.getPatient().getName());
        }
        if (appointment.getDoctor() != null) {
            builder.setDoctorId(appointment.getDoctor().getDoctorId());
            builder.setDoctorName(appointment.getDoctor().getName());
            builder.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
        }
        if (appointment.getSlot() != null) {
            builder.setSlotId(appointment.getSlot().getSlotId());
        }
        if (appointment.getStaff() != null) {
            builder.setStaffId(appointment.getStaff().getStaffId());
        }
        return builder.build();
    }
}

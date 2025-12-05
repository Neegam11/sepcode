package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Appointment;
import com.clinic.datalayer.entities.Doctor;
import com.clinic.datalayer.repositories.AppointmentRepository;
import com.clinic.datalayer.repositories.AvailableSlotRepository;
import com.clinic.datalayer.repositories.DoctorRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * gRPC service implementation for Report generation.
 */
@GRpcService
public class ReportServiceImpl extends ReportServiceGrpc.ReportServiceImplBase {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AvailableSlotRepository availableSlotRepository;

    @Override
    public void generateScheduleReport(ScheduleReportRequest request, StreamObserver<ScheduleReportResponse> responseObserver) {
        try {
            List<Appointment> appointments;
            
            if (request.getDoctorId() > 0) {
                appointments = appointmentRepository.findByDoctorDoctorId(request.getDoctorId());
            } else {
                appointments = appointmentRepository.findAll();
            }

            if (!request.getStartDate().isEmpty() && !request.getEndDate().isEmpty()) {
                LocalDate startDate = LocalDate.parse(request.getStartDate());
                LocalDate endDate = LocalDate.parse(request.getEndDate());
                appointments = appointments.stream()
                    .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                    .collect(Collectors.toList());
            }

            int totalAppointments = appointments.size();
            int completedAppointments = (int) appointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
            int cancelledAppointments = (int) appointments.stream().filter(a -> a.getStatus() != null && a.getStatus().startsWith("CANCELLED")).count();
            int missedAppointments = (int) appointments.stream().filter(a -> "MISSED".equals(a.getStatus())).count();

            Map<Long, List<Appointment>> appointmentsByDoctor = appointments.stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getDoctorId()));

            ScheduleReportResponse.Builder responseBuilder = ScheduleReportResponse.newBuilder()
                .setTotalAppointments(totalAppointments)
                .setCompletedAppointments(completedAppointments)
                .setCancelledAppointments(cancelledAppointments)
                .setMissedAppointments(missedAppointments);

            for (Map.Entry<Long, List<Appointment>> entry : appointmentsByDoctor.entrySet()) {
                Doctor doctor = entry.getValue().get(0).getDoctor();
                int doctorAppointments = entry.getValue().size();
                int availableSlots = availableSlotRepository.findByDoctorDoctorIdAndStatus(doctor.getDoctorId(), "AVAILABLE").size();

                DoctorScheduleSummary summary = DoctorScheduleSummary.newBuilder()
                    .setDoctorId(doctor.getDoctorId())
                    .setDoctorName(doctor.getName())
                    .setTotalAppointments(doctorAppointments)
                    .setAvailableSlots(availableSlots)
                    .build();
                responseBuilder.addDoctorSummaries(summary);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Failed to generate report: " + e.getMessage()).asRuntimeException());
        }
    }
}

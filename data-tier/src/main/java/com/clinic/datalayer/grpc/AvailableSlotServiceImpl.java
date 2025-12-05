package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.AvailableSlot;
import com.clinic.datalayer.repositories.AvailableSlotRepository;
import com.clinic.datalayer.repositories.DoctorRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * gRPC service implementation for AvailableSlot operations.
 */
@GRpcService
public class AvailableSlotServiceImpl extends AvailableSlotServiceGrpc.AvailableSlotServiceImplBase {

    @Autowired
    private AvailableSlotRepository availableSlotRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public void createSlot(AvailableSlotMessage request, StreamObserver<AvailableSlotMessage> responseObserver) {
        doctorRepository.findById(request.getDoctorId())
            .ifPresentOrElse(
                doctor -> {
                    AvailableSlot slot = new AvailableSlot();
                    slot.setDoctor(doctor);
                    slot.setDate(LocalDate.parse(request.getDate()));
                    slot.setStartTime(LocalTime.parse(request.getStartTime()));
                    slot.setEndTime(LocalTime.parse(request.getEndTime()));
                    slot.setStatus("AVAILABLE");
                    AvailableSlot savedSlot = availableSlotRepository.save(slot);
                    responseObserver.onNext(convertToMessage(savedSlot));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Doctor not found").asRuntimeException())
            );
    }

    @Override
    public void createMultipleSlots(CreateSlotsRequest request, StreamObserver<AvailableSlotListResponse> responseObserver) {
        doctorRepository.findById(request.getDoctorId())
            .ifPresentOrElse(
                doctor -> {
                    List<AvailableSlot> createdSlots = new ArrayList<>();
                    LocalDate date = LocalDate.parse(request.getDate());
                    for (TimeSlot timeSlot : request.getTimeSlotsList()) {
                        AvailableSlot slot = new AvailableSlot();
                        slot.setDoctor(doctor);
                        slot.setDate(date);
                        slot.setStartTime(LocalTime.parse(timeSlot.getStartTime()));
                        slot.setEndTime(LocalTime.parse(timeSlot.getEndTime()));
                        slot.setStatus("AVAILABLE");
                        createdSlots.add(availableSlotRepository.save(slot));
                    }
                    AvailableSlotListResponse.Builder builder = AvailableSlotListResponse.newBuilder();
                    createdSlots.forEach(s -> builder.addSlots(convertToMessage(s)));
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Doctor not found").asRuntimeException())
            );
    }

    @Override
    public void getSlotById(IdRequest request, StreamObserver<AvailableSlotMessage> responseObserver) {
        availableSlotRepository.findById(request.getId())
            .ifPresentOrElse(
                slot -> {
                    responseObserver.onNext(convertToMessage(slot));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Slot not found").asRuntimeException())
            );
    }

    @Override
    public void getAllSlots(Empty request, StreamObserver<AvailableSlotListResponse> responseObserver) {
        var slots = availableSlotRepository.findAll();
        AvailableSlotListResponse.Builder builder = AvailableSlotListResponse.newBuilder();
        slots.forEach(s -> builder.addSlots(convertToMessage(s)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSlotsByFilter(SlotFilterRequest request, StreamObserver<AvailableSlotListResponse> responseObserver) {
        List<AvailableSlot> slots;
        if (request.getDoctorId() > 0 && !request.getDate().isEmpty()) {
            slots = availableSlotRepository.findByDoctorDoctorIdAndDate(request.getDoctorId(), LocalDate.parse(request.getDate()));
        } else if (request.getDoctorId() > 0) {
            slots = availableSlotRepository.findByDoctorDoctorId(request.getDoctorId());
        } else if (!request.getDate().isEmpty()) {
            slots = availableSlotRepository.findByDate(LocalDate.parse(request.getDate()));
        } else if (!request.getStatus().isEmpty()) {
            slots = availableSlotRepository.findByStatus(request.getStatus());
        } else {
            slots = availableSlotRepository.findAll();
        }
        AvailableSlotListResponse.Builder builder = AvailableSlotListResponse.newBuilder();
        slots.forEach(s -> builder.addSlots(convertToMessage(s)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableSlots(SlotFilterRequest request, StreamObserver<AvailableSlotListResponse> responseObserver) {
        List<AvailableSlot> slots;
        if (request.getDoctorId() > 0 && !request.getDate().isEmpty()) {
            slots = availableSlotRepository.findAvailableSlotsByDoctorAndDate(request.getDoctorId(), LocalDate.parse(request.getDate()));
        } else if (!request.getDate().isEmpty()) {
            slots = availableSlotRepository.findByDateAndStatus(LocalDate.parse(request.getDate()), "AVAILABLE");
        } else {
            slots = availableSlotRepository.findAllAvailableSlots(LocalDate.now());
        }
        AvailableSlotListResponse.Builder builder = AvailableSlotListResponse.newBuilder();
        slots.forEach(s -> builder.addSlots(convertToMessage(s)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorSlots(IdRequest request, StreamObserver<AvailableSlotListResponse> responseObserver) {
        var slots = availableSlotRepository.findByDoctorDoctorId(request.getId());
        AvailableSlotListResponse.Builder builder = AvailableSlotListResponse.newBuilder();
        slots.forEach(s -> builder.addSlots(convertToMessage(s)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateSlot(AvailableSlotMessage request, StreamObserver<AvailableSlotMessage> responseObserver) {
        availableSlotRepository.findById(request.getSlotId())
            .ifPresentOrElse(
                slot -> {
                    if (!request.getStatus().isEmpty()) slot.setStatus(request.getStatus());
                    AvailableSlot savedSlot = availableSlotRepository.save(slot);
                    responseObserver.onNext(convertToMessage(savedSlot));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Slot not found").asRuntimeException())
            );
    }

    @Override
    public void deleteSlot(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (availableSlotRepository.existsById(request.getId())) {
            availableSlotRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(true).setMessage("Slot deleted").build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(false).setMessage("Slot not found").build());
        }
        responseObserver.onCompleted();
    }

    private AvailableSlotMessage convertToMessage(AvailableSlot slot) {
        AvailableSlotMessage.Builder builder = AvailableSlotMessage.newBuilder()
            .setSlotId(slot.getSlotId())
            .setDate(slot.getDate().toString())
            .setStartTime(slot.getStartTime().toString())
            .setEndTime(slot.getEndTime().toString())
            .setStatus(slot.getStatus() != null ? slot.getStatus() : "");
        if (slot.getDoctor() != null) {
            builder.setDoctorId(slot.getDoctor().getDoctorId());
            builder.setDoctorName(slot.getDoctor().getName());
            builder.setDoctorSpecialization(slot.getDoctor().getSpecialization());
        }
        if (slot.getAppointment() != null) {
            builder.setAppointmentId(slot.getAppointment().getAppointmentId());
        }
        return builder.build();
    }
}

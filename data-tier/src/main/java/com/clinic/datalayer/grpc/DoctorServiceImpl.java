package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Doctor;
import com.clinic.datalayer.repositories.DoctorRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * gRPC service implementation for Doctor operations.
 */
@GRpcService
public class DoctorServiceImpl extends DoctorServiceGrpc.DoctorServiceImplBase {

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public void createDoctor(DoctorMessage request, StreamObserver<DoctorMessage> responseObserver) {
        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(request.getPassword());

        Doctor savedDoctor = doctorRepository.save(doctor);
        responseObserver.onNext(convertToMessage(savedDoctor));
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorById(IdRequest request, StreamObserver<DoctorMessage> responseObserver) {
        doctorRepository.findById(request.getId())
                .ifPresentOrElse(
                        doctor -> {
                            responseObserver.onNext(convertToMessage(doctor));
                            responseObserver.onCompleted();
                        },
                        () -> {
                            responseObserver.onError(
                                    io.grpc.Status.NOT_FOUND
                                            .withDescription("Doctor not found with id: " + request.getId())
                                            .asRuntimeException()
                            );
                        }
                );
    }

    @Override
    public void getDoctorsBySpecialization(DoctorMessage request, StreamObserver<DoctorListResponse> responseObserver) {
        var doctors = doctorRepository.findBySpecialization(request.getSpecialization());

        DoctorListResponse.Builder responseBuilder = DoctorListResponse.newBuilder();
        doctors.forEach(doctor -> responseBuilder.addDoctors(convertToMessage(doctor)));

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllDoctors(Empty request, StreamObserver<DoctorListResponse> responseObserver) {
        var doctors = doctorRepository.findAll();

        DoctorListResponse.Builder responseBuilder = DoctorListResponse.newBuilder();
        doctors.forEach(doctor -> responseBuilder.addDoctors(convertToMessage(doctor)));

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateDoctor(DoctorMessage request, StreamObserver<DoctorMessage> responseObserver) {
        doctorRepository.findById(request.getDoctorId())
                .ifPresentOrElse(
                        doctor -> {
                            if (!request.getName().isEmpty()) {
                                doctor.setName(request.getName());
                            }
                            if (!request.getSpecialization().isEmpty()) {
                                doctor.setSpecialization(request.getSpecialization());
                            }
                            if (!request.getEmail().isEmpty()) {
                                doctor.setEmail(request.getEmail());
                            }
                            if (!request.getPassword().isEmpty()) {
                                doctor.setPassword(request.getPassword());
                            }
                            Doctor savedDoctor = doctorRepository.save(doctor);
                            responseObserver.onNext(convertToMessage(savedDoctor));
                            responseObserver.onCompleted();
                        },
                        () -> {
                            responseObserver.onError(
                                    io.grpc.Status.NOT_FOUND
                                            .withDescription("Doctor not found with id: " + request.getDoctorId())
                                            .asRuntimeException()
                            );
                        }
                );
    }

    @Override
    public void deleteDoctor(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (doctorRepository.existsById(request.getId())) {
            doctorRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Doctor deleted successfully")
                    .build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Doctor not found with id: " + request.getId())
                    .build());
        }
        responseObserver.onCompleted();
    }

    private DoctorMessage convertToMessage(Doctor doctor) {
        return DoctorMessage.newBuilder()
                .setDoctorId(doctor.getDoctorId())
                .setName(doctor.getName() != null ? doctor.getName() : "")
                .setSpecialization(doctor.getSpecialization() != null ? doctor.getSpecialization() : "")
                .setEmail(doctor.getEmail() != null ? doctor.getEmail() : "")
                .build();
    }
}

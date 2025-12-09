package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Patient;
import com.clinic.datalayer.repositories.PatientRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;


@GRpcService
public class PatientServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public void createPatient(PatientMessage request, StreamObserver<PatientMessage> responseObserver) {
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setPassword(request.getPassword());
        
        Patient savedPatient = patientRepository.save(patient);
        responseObserver.onNext(convertToMessage(savedPatient));
        responseObserver.onCompleted();
    }

    @Override
    public void getPatientById(IdRequest request, StreamObserver<PatientMessage> responseObserver) {
        patientRepository.findById(request.getId())
            .ifPresentOrElse(
                patient -> {
                    responseObserver.onNext(convertToMessage(patient));
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                            .withDescription("Patient not found with id: " + request.getId())
                            .asRuntimeException()
                    );
                }
            );
    }

    @Override
    public void getPatientByEmail(PatientMessage request, StreamObserver<PatientMessage> responseObserver) {
        patientRepository.findByEmail(request.getEmail())
            .ifPresentOrElse(
                patient -> {
                    responseObserver.onNext(convertToMessage(patient));
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                            .withDescription("Patient not found with email: " + request.getEmail())
                            .asRuntimeException()
                    );
                }
            );
    }

    @Override
    public void getAllPatients(Empty request, StreamObserver<PatientListResponse> responseObserver) {
        var patients = patientRepository.findAll();
        
        PatientListResponse.Builder responseBuilder = PatientListResponse.newBuilder();
        patients.forEach(patient -> responseBuilder.addPatients(convertToMessage(patient)));
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updatePatient(PatientMessage request, StreamObserver<PatientMessage> responseObserver) {
        patientRepository.findById(request.getPatientId())
            .ifPresentOrElse(
                patient -> {
                    if (!request.getName().isEmpty()) {
                        patient.setName(request.getName());
                    }
                    if (!request.getEmail().isEmpty()) {
                        patient.setEmail(request.getEmail());
                    }
                    if (!request.getPhone().isEmpty()) {
                        patient.setPhone(request.getPhone());
                    }
                    if (!request.getPassword().isEmpty()) {
                        patient.setPassword(request.getPassword());
                    }
                    Patient savedPatient = patientRepository.save(patient);
                    responseObserver.onNext(convertToMessage(savedPatient));
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                            .withDescription("Patient not found with id: " + request.getPatientId())
                            .asRuntimeException()
                    );
                }
            );
    }

    @Override
    public void deletePatient(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (patientRepository.existsById(request.getId())) {
            patientRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Patient deleted successfully")
                .build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Patient not found with id: " + request.getId())
                .build());
        }
        responseObserver.onCompleted();
    }

    private PatientMessage convertToMessage(Patient patient) {
        return PatientMessage.newBuilder()
            .setPatientId(patient.getPatientId())
            .setName(patient.getName() != null ? patient.getName() : "")
            .setEmail(patient.getEmail() != null ? patient.getEmail() : "")
            .setPhone(patient.getPhone() != null ? patient.getPhone() : "")
            .build();
    }
}

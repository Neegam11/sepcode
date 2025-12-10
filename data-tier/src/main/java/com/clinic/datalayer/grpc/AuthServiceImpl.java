package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Doctor;
import com.clinic.datalayer.entities.Patient;
import com.clinic.datalayer.entities.Staff;
import com.clinic.datalayer.repositories.DoctorRepository;
import com.clinic.datalayer.repositories.PatientRepository;
import com.clinic.datalayer.repositories.StaffRepository;
import com.clinic.datalayer.util.PasswordUtil;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String email = request.getEmail();
        String password = request.getPassword();
        String userType = request.getUserType();

        LoginResponse.Builder responseBuilder = LoginResponse.newBuilder();

        try {
            switch (userType.toUpperCase()) {
                case "PATIENT":
                    patientRepository.findByEmail(email)
                            .ifPresentOrElse(
                                    patient -> {
                                        if (PasswordUtil.verifyPassword(password, patient.getPassword())) {
                                            responseBuilder.setSuccess(true)
                                                    .setMessage("Login successful")
                                                    .setUserId(patient.getPatientId())
                                                    .setUserType("PATIENT")
                                                    .setName(patient.getName())
                                                    .setToken(generateToken(patient.getPatientId(), "PATIENT"));
                                        } else {
                                            responseBuilder.setSuccess(false)
                                                    .setMessage("Invalid email or password");
                                        }
                                    },
                                    () -> {
                                        responseBuilder.setSuccess(false)
                                                .setMessage("Invalid email or password");
                                    }
                            );
                    break;

                case "DOCTOR":
                    doctorRepository.findByEmail(email)
                            .ifPresentOrElse(
                                    doctor -> {
                                        if (PasswordUtil.verifyPassword(password, doctor.getPassword())) {
                                            responseBuilder.setSuccess(true)
                                                    .setMessage("Login successful")
                                                    .setUserId(doctor.getDoctorId())
                                                    .setUserType("DOCTOR")
                                                    .setName(doctor.getName())
                                                    .setToken(generateToken(doctor.getDoctorId(), "DOCTOR"));
                                        } else {
                                            responseBuilder.setSuccess(false)
                                                    .setMessage("Invalid email or password");
                                        }
                                    },
                                    () -> {
                                        responseBuilder.setSuccess(false)
                                                .setMessage("Invalid email or password");
                                    }
                            );
                    break;

                case "STAFF":
                    staffRepository.findByEmail(email)
                            .ifPresentOrElse(
                                    staff -> {
                                        if (PasswordUtil.verifyPassword(password, staff.getPassword())) {
                                            responseBuilder.setSuccess(true)
                                                    .setMessage("Login successful")
                                                    .setUserId(staff.getStaffId())
                                                    .setUserType("STAFF")
                                                    .setName(staff.getName())
                                                    .setToken(generateToken(staff.getStaffId(), "STAFF"));
                                        } else {
                                            responseBuilder.setSuccess(false)
                                                    .setMessage("Invalid email or password");
                                        }
                                    },
                                    () -> {
                                        responseBuilder.setSuccess(false)
                                                .setMessage("Invalid email or password");
                                    }
                            );
                    break;

                default:
                    responseBuilder.setSuccess(false)
                            .setMessage("Invalid user type");
            }
        } catch (Exception e) {
            responseBuilder.setSuccess(false)
                    .setMessage("Login failed: " + e.getMessage());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<StatusResponse> responseObserver) {
        StatusResponse.Builder responseBuilder = StatusResponse.newBuilder();

        try {
            switch (request.getUserType().toUpperCase()) {
                case "PATIENT":
                    if (patientRepository.existsByEmail(request.getEmail())) {
                        responseBuilder.setSuccess(false)
                                .setMessage("Email already registered");
                    } else {
                        Patient patient = new Patient();
                        patient.setName(request.getName());
                        patient.setEmail(request.getEmail());
                        patient.setPhone(request.getPhone());
                        patient.setPassword(PasswordUtil.hashPassword(request.getPassword()));
                        patientRepository.save(patient);
                        responseBuilder.setSuccess(true)
                                .setMessage("Patient registered successfully");
                    }
                    break;

                case "DOCTOR":
                    if (doctorRepository.existsByEmail(request.getEmail())) {
                        responseBuilder.setSuccess(false)
                                .setMessage("Email already registered");
                    } else {
                        Doctor doctor = new Doctor();
                        doctor.setName(request.getName());
                        doctor.setEmail(request.getEmail());
                        doctor.setSpecialization(request.getSpecialization());
                        doctor.setPassword(PasswordUtil.hashPassword(request.getPassword()));
                        doctorRepository.save(doctor);
                        responseBuilder.setSuccess(true)
                                .setMessage("Doctor registered successfully");
                    }
                    break;

                case "STAFF":
                    if (staffRepository.existsByEmail(request.getEmail())) {
                        responseBuilder.setSuccess(false)
                                .setMessage("Email already registered");
                    } else {
                        Staff staff = new Staff();
                        staff.setName(request.getName());
                        staff.setEmail(request.getEmail());
                        staff.setRole(request.getRole());
                        staff.setPassword(PasswordUtil.hashPassword(request.getPassword()));
                        staffRepository.save(staff);
                        responseBuilder.setSuccess(true)
                                .setMessage("Staff registered successfully");
                    }
                    break;

                default:
                    responseBuilder.setSuccess(false)
                            .setMessage("Invalid user type");
            }
        } catch (Exception e) {
            responseBuilder.setSuccess(false)
                    .setMessage("Registration failed: " + e.getMessage());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void validateToken(IdRequest request, StreamObserver<LoginResponse> responseObserver) {
        responseObserver.onNext(LoginResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Token valid")
                .build());
        responseObserver.onCompleted();
    }

    private String generateToken(Long userId, String userType) {
        return userType + "_" + userId + "_" + System.currentTimeMillis();
    }
}
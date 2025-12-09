package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Staff;
import com.clinic.datalayer.repositories.StaffRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;


@GRpcService
public class StaffServiceImpl extends StaffServiceGrpc.StaffServiceImplBase {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public void createStaff(StaffMessage request, StreamObserver<StaffMessage> responseObserver) {
        Staff staff = new Staff();
        staff.setName(request.getName());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setPassword(request.getPassword());
        
        Staff savedStaff = staffRepository.save(staff);
        responseObserver.onNext(convertToMessage(savedStaff));
        responseObserver.onCompleted();
    }

    @Override
    public void getStaffById(IdRequest request, StreamObserver<StaffMessage> responseObserver) {
        staffRepository.findById(request.getId())
            .ifPresentOrElse(
                staff -> {
                    responseObserver.onNext(convertToMessage(staff));
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                            .withDescription("Staff not found with id: " + request.getId())
                            .asRuntimeException()
                    );
                }
            );
    }

    @Override
    public void getAllStaff(Empty request, StreamObserver<StaffListResponse> responseObserver) {
        var staffList = staffRepository.findAll();
        
        StaffListResponse.Builder responseBuilder = StaffListResponse.newBuilder();
        staffList.forEach(staff -> responseBuilder.addStaffList(convertToMessage(staff)));
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateStaff(StaffMessage request, StreamObserver<StaffMessage> responseObserver) {
        staffRepository.findById(request.getStaffId())
            .ifPresentOrElse(
                staff -> {
                    if (!request.getName().isEmpty()) {
                        staff.setName(request.getName());
                    }
                    if (!request.getEmail().isEmpty()) {
                        staff.setEmail(request.getEmail());
                    }
                    if (!request.getRole().isEmpty()) {
                        staff.setRole(request.getRole());
                    }
                    if (!request.getPassword().isEmpty()) {
                        staff.setPassword(request.getPassword());
                    }
                    Staff savedStaff = staffRepository.save(staff);
                    responseObserver.onNext(convertToMessage(savedStaff));
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                            .withDescription("Staff not found with id: " + request.getStaffId())
                            .asRuntimeException()
                    );
                }
            );
    }

    @Override
    public void deleteStaff(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (staffRepository.existsById(request.getId())) {
            staffRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Staff deleted successfully")
                .build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Staff not found with id: " + request.getId())
                .build());
        }
        responseObserver.onCompleted();
    }

    private StaffMessage convertToMessage(Staff staff) {
        return StaffMessage.newBuilder()
            .setStaffId(staff.getStaffId())
            .setName(staff.getName() != null ? staff.getName() : "")
            .setEmail(staff.getEmail() != null ? staff.getEmail() : "")
            .setRole(staff.getRole() != null ? staff.getRole() : "")
            .build();
    }
}

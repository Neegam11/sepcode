package com.clinic.datalayer.grpc;

import com.clinic.datalayer.entities.Notification;
import com.clinic.datalayer.repositories.AppointmentRepository;
import com.clinic.datalayer.repositories.NotificationRepository;
import com.clinic.datalayer.repositories.StaffRepository;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@GRpcService
public class NotificationServiceImpl extends NotificationServiceGrpc.NotificationServiceImplBase {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public void sendNotification(SendNotificationRequest request, StreamObserver<NotificationMessage> responseObserver) {
        try {
            Notification notification = new Notification();
            notification.setRecipientId(request.getRecipientId());
            notification.setRecipientType(request.getRecipientType());
            notification.setMessage(request.getMessage());
            notification.setType(request.getType().isEmpty() ? "MANUAL" : request.getType());
            notification.setStatus("SENT");
            notification.setChannel(request.getChannel().isEmpty() ? "EMAIL" : request.getChannel());

            if (request.getAppointmentId() > 0) {
                appointmentRepository.findById(request.getAppointmentId()).ifPresent(notification::setAppointment);
            }
            if (request.getStaffId() > 0) {
                staffRepository.findById(request.getStaffId()).ifPresent(notification::setStaff);
            }

            Notification savedNotification = notificationRepository.save(notification);
            responseObserver.onNext(convertToMessage(savedNotification));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Failed to send notification: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getNotificationById(IdRequest request, StreamObserver<NotificationMessage> responseObserver) {
        notificationRepository.findById(request.getId())
            .ifPresentOrElse(
                notification -> {
                    responseObserver.onNext(convertToMessage(notification));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Notification not found").asRuntimeException())
            );
    }

    @Override
    public void getAllNotifications(Empty request, StreamObserver<NotificationListResponse> responseObserver) {
        var notifications = notificationRepository.findAll();
        NotificationListResponse.Builder builder = NotificationListResponse.newBuilder();
        notifications.forEach(n -> builder.addNotifications(convertToMessage(n)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getNotificationsByFilter(NotificationFilterRequest request, StreamObserver<NotificationListResponse> responseObserver) {
        List<Notification> notifications;
        if (request.getRecipientId() > 0 && !request.getRecipientType().isEmpty()) {
            notifications = notificationRepository.findByRecipientIdAndRecipientType(request.getRecipientId(), request.getRecipientType());
        } else if (!request.getStatus().isEmpty()) {
            notifications = notificationRepository.findByStatus(request.getStatus());
        } else {
            notifications = notificationRepository.findAll();
        }
        NotificationListResponse.Builder builder = NotificationListResponse.newBuilder();
        notifications.forEach(n -> builder.addNotifications(convertToMessage(n)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserNotifications(NotificationFilterRequest request, StreamObserver<NotificationListResponse> responseObserver) {
        var notifications = notificationRepository.findByRecipientIdAndRecipientType(request.getRecipientId(), request.getRecipientType());
        NotificationListResponse.Builder builder = NotificationListResponse.newBuilder();
        notifications.forEach(n -> builder.addNotifications(convertToMessage(n)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void markNotificationAsRead(IdRequest request, StreamObserver<NotificationMessage> responseObserver) {
        notificationRepository.findById(request.getId())
            .ifPresentOrElse(
                notification -> {
                    notification.setStatus("READ");
                    Notification savedNotification = notificationRepository.save(notification);
                    responseObserver.onNext(convertToMessage(savedNotification));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Notification not found").asRuntimeException())
            );
    }

    @Override
    public void deleteNotification(IdRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (notificationRepository.existsById(request.getId())) {
            notificationRepository.deleteById(request.getId());
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(true).setMessage("Notification deleted").build());
        } else {
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(false).setMessage("Notification not found").build());
        }
        responseObserver.onCompleted();
    }

    private NotificationMessage convertToMessage(Notification notification) {
        NotificationMessage.Builder builder = NotificationMessage.newBuilder()
            .setNotificationId(notification.getNotificationId())
            .setRecipientId(notification.getRecipientId())
            .setRecipientType(notification.getRecipientType() != null ? notification.getRecipientType() : "")
            .setMessage(notification.getMessage() != null ? notification.getMessage() : "")
            .setType(notification.getType() != null ? notification.getType() : "")
            .setStatus(notification.getStatus() != null ? notification.getStatus() : "")
            .setChannel(notification.getChannel() != null ? notification.getChannel() : "")
            .setCreatedAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : "");
        if (notification.getAppointment() != null) {
            builder.setAppointmentId(notification.getAppointment().getAppointmentId());
        }
        if (notification.getStaff() != null) {
            builder.setStaffId(notification.getStaff().getStaffId());
        }
        return builder.build();
    }
}

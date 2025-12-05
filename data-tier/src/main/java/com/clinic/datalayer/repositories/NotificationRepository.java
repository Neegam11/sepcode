package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for Notification entity operations.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, String recipientType);
    List<Notification> findByRecipientIdAndRecipientTypeAndStatus(Long recipientId, String recipientType, String status);
    List<Notification> findByAppointmentAppointmentId(Long appointmentId);
    List<Notification> findByStaffStaffId(Long staffId);
    List<Notification> findByStatus(String status);
    List<Notification> findByType(String type);
}

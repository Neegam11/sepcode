package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Appointment entity operations.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientPatientId(Long patientId);
    List<Appointment> findByDoctorDoctorId(Long doctorId);
    List<Appointment> findByStaffStaffId(Long staffId);
    List<Appointment> findByStatus(String status);
    List<Appointment> findByDate(LocalDate date);
    
    List<Appointment> findByDoctorDoctorIdAndDate(Long doctorId, LocalDate date);
    List<Appointment> findByPatientPatientIdAndStatus(Long patientId, String status);
    List<Appointment> findByDoctorDoctorIdAndStatus(Long doctorId, String status);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.doctorId = :doctorId AND a.date = :date AND a.status NOT IN ('CANCELLED_BY_PATIENT', 'CANCELLED_BY_DOCTOR')")
    List<Appointment> findActiveDoctorAppointmentsByDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY a.date, a.startTime")
    List<Appointment> findAllUpcomingAppointments();
    
    @Query("SELECT a FROM Appointment a WHERE a.status = 'SCHEDULED' AND a.cancellationReason IS NOT NULL")
    List<Appointment> findPendingCancellations();
}

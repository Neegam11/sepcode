package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.AvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for AvailableSlot entity operations.
 */
public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, Long> {
    List<AvailableSlot> findByDoctorDoctorId(Long doctorId);
    List<AvailableSlot> findByDate(LocalDate date);
    List<AvailableSlot> findByStatus(String status);
    List<AvailableSlot> findByDoctorDoctorIdAndDate(Long doctorId, LocalDate date);
    List<AvailableSlot> findByDoctorDoctorIdAndStatus(Long doctorId, String status);
    List<AvailableSlot> findByDateAndStatus(LocalDate date, String status);
    
    @Query("SELECT s FROM AvailableSlot s WHERE s.doctor.doctorId = :doctorId AND s.date = :date AND s.status = 'AVAILABLE'")
    List<AvailableSlot> findAvailableSlotsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT s FROM AvailableSlot s WHERE s.date >= :startDate AND s.status = 'AVAILABLE' ORDER BY s.date, s.startTime")
    List<AvailableSlot> findAllAvailableSlots(@Param("startDate") LocalDate startDate);
}

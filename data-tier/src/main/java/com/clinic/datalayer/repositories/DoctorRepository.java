package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
}

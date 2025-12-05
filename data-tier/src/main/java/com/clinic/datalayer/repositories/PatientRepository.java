package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for Patient entity operations.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
}

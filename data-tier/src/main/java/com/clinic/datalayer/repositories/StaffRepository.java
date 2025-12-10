package com.clinic.datalayer.repositories;

import com.clinic.datalayer.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByEmailAndPassword(String email, String password);
    List<Staff> findByRole(String role);
    boolean existsByEmail(String email);
}

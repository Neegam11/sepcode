package com.clinic.datalayer;

import com.clinic.datalayer.entities.Doctor;
import com.clinic.datalayer.entities.Staff;
import com.clinic.datalayer.repositories.DoctorRepository;
import com.clinic.datalayer.repositories.StaffRepository;
import com.clinic.datalayer.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final DoctorRepository doctorRepository;
    private final StaffRepository staffRepository;

    public DataLoader(DoctorRepository doctorRepository, StaffRepository staffRepository) {
        this.doctorRepository = doctorRepository;
        this.staffRepository = staffRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        loadDoctors();
        loadStaff();
        System.out.println("DataLoader: Sample doctors and staff have been loaded successfully!");
        System.out.println("DataLoader: doctors and staff have been added to the database");
    }
    private void loadDoctors() {
        if (doctorRepository.count() == 0) {
            // Doctor 1 - Email: sarah.johnson@clinic.com | Password: doctor@143
            Doctor doctor1 = new Doctor();
            doctor1.setName("Dr. Sarah Johnson");
            doctor1.setEmail("sarah.johnson@clinic.com");
            doctor1.setPassword(PasswordUtil.hashPassword("doctor@143"));
            doctor1.setSpecialization("Cardiology");
            doctorRepository.save(doctor1);


            Doctor doctor2 = new Doctor();
            doctor2.setName("Dr. Michael Chen");
            doctor2.setEmail("michael.chen@clinic.com");
            doctor2.setPassword(PasswordUtil.hashPassword("doctor@123"));
            doctor2.setSpecialization("Neurology");
            doctorRepository.save(doctor2);


            Doctor doctor3 = new Doctor();
            doctor3.setName("Dr. Emily Davis");
            doctor3.setEmail("emily.davis@clinic.com");
            doctor3.setPassword(PasswordUtil.hashPassword("doctor@000"));
            doctor3.setSpecialization("Pediatrics");
            doctorRepository.save(doctor3);


            Doctor doctor4 = new Doctor();
            doctor4.setName("Dr. James Wilson");
            doctor4.setEmail("james.wilson@clinic.com");
            doctor4.setPassword(PasswordUtil.hashPassword("doctor@888"));
            doctor4.setSpecialization("Orthopedics");
            doctorRepository.save(doctor4);


            Doctor doctor5 = new Doctor();
            doctor5.setName("Dr. Lisa Anderson");
            doctor5.setEmail("lisa.anderson@clinic.com");
            doctor5.setPassword(PasswordUtil.hashPassword("doctor@978"));
            doctor5.setSpecialization("Dermatology");
            doctorRepository.save(doctor5);


            Doctor doctor6 = new Doctor();
            doctor6.setName("Dr. Robert Brown");
            doctor6.setEmail("robert.brown@clinic.com");
            doctor6.setPassword(PasswordUtil.hashPassword("doctor@456"));
            doctor6.setSpecialization("General");
            doctorRepository.save(doctor6);

            System.out.println("Doctors added into the database (passwords hashed with BCrypt)");
        } else {
            System.out.println("Doctors already exist in database, skipping seed data");
        }
    }

    private void loadStaff() {
        if (staffRepository.count() == 0) {
            // Staff 1 - Email: admin@clinic.com | Password: admin123
            Staff staff1 = new Staff();
            staff1.setName("Admin User");
            staff1.setEmail("admin@clinic.com");
            staff1.setPassword(PasswordUtil.hashPassword("admin123"));
            staff1.setRole("Administrator");
            staffRepository.save(staff1);

            // Staff 2 - Email: jane.smith@clinic.com | Password: staff123
            Staff staff2 = new Staff();
            staff2.setName("Jane Smith");
            staff2.setEmail("jane.smith@clinic.com");
            staff2.setPassword(PasswordUtil.hashPassword("staff123"));
            staff2.setRole("Receptionist");
            staffRepository.save(staff2);

            // Staff 3 - Email: mark.thompson@clinic.com | Password: staff123
            Staff staff3 = new Staff();
            staff3.setName("Mark Thompson");
            staff3.setEmail("mark.thompson@clinic.com");
            staff3.setPassword(PasswordUtil.hashPassword("staff123"));
            staff3.setRole("Receptionist");
            staffRepository.save(staff3);

            // Staff 4 - Email: susan.clark@clinic.com | Password: staff123
            Staff staff4 = new Staff();
            staff4.setName("Susan Clark");
            staff4.setEmail("susan.clark@clinic.com");
            staff4.setPassword(PasswordUtil.hashPassword("staff123"));
            staff4.setRole("Manager");
            staffRepository.save(staff4);

            System.out.println("Staff members added into the database (passwords hashed with BCrypt)");
        } else {
            System.out.println("Staff already exist in database, skipping seed data");
        }
    }
}
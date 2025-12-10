package com.clinic.datalayer;
import com.clinic.datalayer.entities.Doctor;
import com.clinic.datalayer.entities.Staff;
import com.clinic.datalayer.repositories.DoctorRepository;
import com.clinic.datalayer.repositories.StaffRepository;
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
        System.out.println(" DataLoader: Sample doctors and staff have been loaded successfully!");
    }

    private void loadDoctors() {
        // Only load if no doctors exist (avoid duplicates on restart)
        if (doctorRepository.count() == 0) {

            // Dr. Sarah Johnson - Cardiologist
            Doctor doctor1 = new Doctor();
            doctor1.setName("Dr. Sarah Johnson");
            doctor1.setEmail("sarah.johnson@clinic.com");
            doctor1.setPassword("doctor123");
            doctor1.setSpecialization("Cardiology");
            doctorRepository.save(doctor1);

            // Dr. Michael Chen - Neurologist
            Doctor doctor2 = new Doctor();
            doctor2.setName("Dr. Michael Chen");
            doctor2.setEmail("michael.chen@clinic.com");
            doctor2.setPassword("doctor123");
            doctor2.setSpecialization("Neurology");
            doctorRepository.save(doctor2);

            // Dr. Emily Davis - Pediatrician
            Doctor doctor3 = new Doctor();
            doctor3.setName("Dr. Emily Davis");
            doctor3.setEmail("emily.davis@clinic.com");
            doctor3.setPassword("doctor123");
            doctor3.setSpecialization("Pediatrics");
            doctorRepository.save(doctor3);

            // Dr. James Wilson - Orthopedic Specialist
            Doctor doctor4 = new Doctor();
            doctor4.setName("Dr. James Wilson");
            doctor4.setEmail("james.wilson@clinic.com");
            doctor4.setPassword("doctor123");
            doctor4.setSpecialization("Orthopedics");
            doctorRepository.save(doctor4);

            // Dr. Lisa Anderson - Dermatologist
            Doctor doctor5 = new Doctor();
            doctor5.setName("Dr. Lisa Anderson");
            doctor5.setEmail("lisa.anderson@clinic.com");
            doctor5.setPassword("doctor123");
            doctor5.setSpecialization("Dermatology");
            doctorRepository.save(doctor5);

            // Dr. Robert Brown - General Practitioner
            Doctor doctor6 = new Doctor();
            doctor6.setName("Dr. Robert Brown");
            doctor6.setEmail("robert.brown@clinic.com");
            doctor6.setPassword("doctor123");
            doctor6.setSpecialization("General");
            doctorRepository.save(doctor6);

            System.out.println(" Loaded 6 doctors into the database");
        } else {
            System.out.println(" Doctors already exist in database, skipping seed data");
        }
    }

    private void loadStaff() {
        // Only load if no staff exist (avoid duplicates on restart)
        if (staffRepository.count() == 0) {

            // Admin User - Administrator
            Staff staff1 = new Staff();
            staff1.setName("Admin User");
            staff1.setEmail("admin@clinic.com");
            staff1.setPassword("admin123");
            staff1.setRole("Administrator");
            staffRepository.save(staff1);

            // Jane Smith - Receptionist
            Staff staff2 = new Staff();
            staff2.setName("Jane Smith");
            staff2.setEmail("jane.smith@clinic.com");
            staff2.setPassword("staff123");
            staff2.setRole("Receptionist");
            staffRepository.save(staff2);

            // Mark Thompson - Receptionist
            Staff staff3 = new Staff();
            staff3.setName("Mark Thompson");
            staff3.setEmail("mark.thompson@clinic.com");
            staff3.setPassword("staff123");
            staff3.setRole("Receptionist");
            staffRepository.save(staff3);

            // Susan Clark - Manager
            Staff staff4 = new Staff();
            staff4.setName("Susan Clark");
            staff4.setEmail("susan.clark@clinic.com");
            staff4.setPassword("staff123");
            staff4.setRole("Manager");
            staffRepository.save(staff4);

            System.out.println(" Loaded 4 staff members into the database");
        } else {
            System.out.println(" Staff already exist in database, skipping seed data");
        }
    }
}


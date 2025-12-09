namespace ClinicClient.Models;
public class AppointmentModel
{
    public long AppointmentId { get; set; }
    public long PatientId { get; set; }
    public long DoctorId { get; set; }
    public long SlotId { get; set; }
    public string Date { get; set; } = "";
    public string StartTime { get; set; } = "";
    public string EndTime { get; set; } = "";
    public string Status { get; set; } = "";
    public string Type { get; set; } = "";
    public long StaffId { get; set; }
    public string CancellationReason { get; set; } = "";
    public string PatientName { get; set; } = "";
    public string DoctorName { get; set; } = "";
    public string DoctorSpecialization { get; set; } = "";
}

public class BookAppointmentModel
{
    public long PatientId { get; set; }
    public long DoctorId { get; set; }
    public long SlotId { get; set; }
    public string Type { get; set; } = "CONSULTATION";
}

public class CancelAppointmentModel
{
    public long AppointmentId { get; set; }
    public string CancelledBy { get; set; } = "";
    public string Reason { get; set; } = "";
}


namespace ClinicClient.Models;

/// <summary>
/// Model representing an available time slot for appointments.
/// </summary>
public class SlotModel
{
    public long SlotId { get; set; }
    public long DoctorId { get; set; }
    public string Date { get; set; } = "";
    public string StartTime { get; set; } = "";
    public string EndTime { get; set; } = "";
    public string Status { get; set; } = "";
    public long AppointmentId { get; set; }
    public string DoctorName { get; set; } = "";
    public string DoctorSpecialization { get; set; } = "";
}

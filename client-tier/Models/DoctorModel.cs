namespace ClinicClient.Models;

/// <summary>
/// Model representing a doctor in the clinic system.
/// </summary>
public class DoctorModel
{
    public long DoctorId { get; set; }
    public string Name { get; set; } = "";
    public string Specialization { get; set; } = "";
    public string Email { get; set; } = "";
}

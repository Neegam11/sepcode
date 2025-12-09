namespace ClinicClient.Models;

public class NotificationModel
{
    public long NotificationId { get; set; }
    public long AppointmentId { get; set; }
    public long StaffId { get; set; }
    public long RecipientId { get; set; }
    public string RecipientType { get; set; } = "";
    public string Message { get; set; } = "";
    public string Type { get; set; } = "";
    public string Status { get; set; } = "";
    public string Channel { get; set; } = "";
    public string CreatedAt { get; set; } = "";
}


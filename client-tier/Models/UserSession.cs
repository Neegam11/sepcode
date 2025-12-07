namespace ClinicClient.Models;

public class UserSession
{
    public long UserId { get; set; }
    public string UserType { get; set; } = "";
    public string Name { get; set; } = "";
    public string Token { get; set; } = "";
    public bool IsAuthenticated => UserId > 0 && !string.IsNullOrEmpty(Token);
}


using System.Net.Http.Json;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class NotificationService
{
    private readonly HttpClient _httpClient;

    public NotificationService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }


    public async Task<List<NotificationModel>> GetUserNotificationsAsync(string userType, long userId)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<NotificationModel>>>(
                $"notifications?recipientType={userType}&recipientId={userId}");
            return response?.Data ?? new List<NotificationModel>();
        }
        catch
        {
            return new List<NotificationModel>();
        }
    }

    // PATCH /api/notifications/{id} - RESTful: PATCH for partial update
    public async Task<bool> MarkAsReadAsync(long notificationId)
    {
        try
        {
            var response = await _httpClient.PatchAsJsonAsync($"notifications/{notificationId}",
                new { status = "READ" });
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }


    public async Task<(bool success, string message)> SendNotificationAsync(NotificationModel notification)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("notifications", notification);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<NotificationModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to send");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }
}


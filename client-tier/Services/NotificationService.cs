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
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<NotificationModel>>>($"notifications/user/{userType}/{userId}");
            return response?.Data ?? new List<NotificationModel>();
        }
        catch
        {
            return new List<NotificationModel>();
        }
    }

    public async Task<bool> MarkAsReadAsync(long notificationId)
    {
        try
        {
            var response = await _httpClient.PatchAsync($"notifications/{notificationId}/read", null);
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
            var response = await _httpClient.PostAsJsonAsync("notifications/send", notification);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<NotificationModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to send");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }
}


using System.Net.Http.Json;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class StaffService
{
    private readonly HttpClient _httpClient;

    public StaffService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<List<AppointmentModel>> GetAllAppointmentsAsync()
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<AppointmentModel>>>("staff/appointments");
            return response?.Data ?? new List<AppointmentModel>();
        }
        catch
        {
            return new List<AppointmentModel>();
        }
    }


    public async Task<(bool success, string message)> ReassignAppointmentAsync(long appointmentId, long? newDoctorId, long? newSlotId)
    {
        try
        {
            var response = await _httpClient.PatchAsJsonAsync($"staff/appointments/{appointmentId}", new
            {
                doctorId = newDoctorId ?? 0,
                slotId = newSlotId ?? 0
            });
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<AppointmentModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to reassign");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }


    public async Task<(bool success, string message)> UpdateAppointmentAsync(long appointmentId, string? status, string? type)
    {
        try
        {
            var body = new Dictionary<string, object>();
            if (!string.IsNullOrEmpty(status)) body["status"] = status;
            if (!string.IsNullOrEmpty(type)) body["type"] = type;

            var response = await _httpClient.PatchAsJsonAsync($"staff/appointments/{appointmentId}", body);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<AppointmentModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to update");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }


    public async Task<(bool success, string message)> CancelAppointmentAsync(long appointmentId, string reason)
    {
        try
        {
            var request = new HttpRequestMessage(HttpMethod.Delete, $"staff/appointments/{appointmentId}")
            {
                Content = JsonContent.Create(new { reason })
            };
            var response = await _httpClient.SendAsync(request);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<AppointmentModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to cancel");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }

    public async Task<(bool success, string message)> CreateSlotAsync(SlotModel slot)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("staff/slots", slot);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<SlotModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to create slot");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }

    public async Task<(bool success, string message)> DeleteSlotAsync(long slotId)
    {
        try
        {
            var response = await _httpClient.DeleteAsync($"staff/slots/{slotId}");
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<object>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to delete slot");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }

    public async Task<Dictionary<string, object>?> GenerateReportAsync(string? startDate, string? endDate, long? doctorId)
    {
        try
        {
            var url = "staff/reports/schedule";
            var queryParams = new List<string>();
            if (!string.IsNullOrEmpty(startDate)) queryParams.Add($"startDate={startDate}");
            if (!string.IsNullOrEmpty(endDate)) queryParams.Add($"endDate={endDate}");
            if (doctorId.HasValue) queryParams.Add($"doctorId={doctorId}");
            if (queryParams.Any()) url += "?" + string.Join("&", queryParams);

            var response = await _httpClient.GetFromJsonAsync<ApiResponse<Dictionary<string, object>>>(url);
            return response?.Data;
        }
        catch
        {
            return null;
        }
    }
}


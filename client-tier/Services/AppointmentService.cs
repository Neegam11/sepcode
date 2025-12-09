using System.Net.Http.Json;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class AppointmentService
{
    private readonly HttpClient _httpClient;

    public AppointmentService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<List<AppointmentModel>> GetAllAppointmentsAsync()
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<AppointmentModel>>>("appointments");
            return response?.Data ?? new List<AppointmentModel>();
        }
        catch
        {
            return new List<AppointmentModel>();
        }
    }

    public async Task<List<AppointmentModel>> GetPatientAppointmentsAsync(long patientId)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<AppointmentModel>>>($"patients/{patientId}/appointments");
            return response?.Data ?? new List<AppointmentModel>();
        }
        catch
        {
            return new List<AppointmentModel>();
        }
    }

    public async Task<List<AppointmentModel>> GetDoctorAppointmentsAsync(long doctorId)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<AppointmentModel>>>($"doctors/{doctorId}/appointments");
            return response?.Data ?? new List<AppointmentModel>();
        }
        catch
        {
            return new List<AppointmentModel>();
        }
    }

    public async Task<List<AppointmentModel>> GetDoctorDailyScheduleAsync(long doctorId, string? date = null)
    {
        try
        {
            var url = $"doctors/{doctorId}/schedule";
            if (!string.IsNullOrEmpty(date)) url += $"?date={date}";
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<AppointmentModel>>>(url);
            return response?.Data ?? new List<AppointmentModel>();
        }
        catch
        {
            return new List<AppointmentModel>();
        }
    }

    // POST /api/appointments - RESTful: POST to collection creates a new resource
    public async Task<(bool success, string message, AppointmentModel? appointment)> BookAppointmentAsync(BookAppointmentModel model)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("appointments", model);
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<AppointmentModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to book", result?.Data);
        }
        catch (Exception ex)
        {
            return (false, ex.Message, null);
        }
    }


    public async Task<(bool success, string message)> CancelAppointmentAsync(long appointmentId, string cancelledBy, string reason)
    {
        try
        {
            var request = new HttpRequestMessage(HttpMethod.Delete, $"appointments/{appointmentId}")
            {
                Content = JsonContent.Create(new { cancelledBy, reason })
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

    // PATCH /api/appointments/{id} - RESTful: PATCH for partial updates
    public async Task<(bool success, string message)> UpdateAppointmentStatusAsync(long appointmentId, string status, long staffId = 0)
    {
        try
        {
            var response = await _httpClient.PatchAsJsonAsync($"appointments/{appointmentId}", new
            {
                status,
                staffId
            });
            var result = await response.Content.ReadFromJsonAsync<ApiResponse<AppointmentModel>>();
            return (result?.Success ?? false, result?.Message ?? "Failed to update");
        }
        catch (Exception ex)
        {
            return (false, ex.Message);
        }
    }


    public async Task<List<SlotModel>> GetAvailableSlotsAsync(long? doctorId = null, string? date = null)
    {
        try
        {
            var queryParams = new List<string> { "status=available" };
            if (doctorId.HasValue) queryParams.Add($"doctorId={doctorId}");
            if (!string.IsNullOrEmpty(date)) queryParams.Add($"date={date}");
            var url = "slots?" + string.Join("&", queryParams);

            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<SlotModel>>>(url);
            return response?.Data ?? new List<SlotModel>();
        }
        catch
        {
            return new List<SlotModel>();
        }
    }
}


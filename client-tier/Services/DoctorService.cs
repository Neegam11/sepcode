using System.Net.Http.Json;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class DoctorService
{
    private readonly HttpClient _httpClient;

    public DoctorService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<List<DoctorModel>> GetAllDoctorsAsync()
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<DoctorModel>>>("doctors");
            return response?.Data ?? new List<DoctorModel>();
        }
        catch
        {
            return new List<DoctorModel>();
        }
    }

    public async Task<DoctorModel?> GetDoctorByIdAsync(long doctorId)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<DoctorModel>>($"doctors/{doctorId}");
            return response?.Data;
        }
        catch
        {
            return null;
        }
    }

    public async Task<List<DoctorModel>> GetDoctorsBySpecializationAsync(string specialization)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<DoctorModel>>>($"doctors/specialization/{specialization}");
            return response?.Data ?? new List<DoctorModel>();
        }
        catch
        {
            return new List<DoctorModel>();
        }
    }

    public async Task<List<SlotModel>> GetDoctorSlotsAsync(long doctorId)
    {
        try
        {
            var response = await _httpClient.GetFromJsonAsync<ApiResponse<List<SlotModel>>>($"doctors/{doctorId}/slots");
            return response?.Data ?? new List<SlotModel>();
        }
        catch
        {
            return new List<SlotModel>();
        }
    }
}


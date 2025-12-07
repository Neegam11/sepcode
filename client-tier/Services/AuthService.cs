using System.Net.Http.Json;
using Blazored.LocalStorage;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class AuthService
{
    private readonly HttpClient _httpClient;
    private readonly ILocalStorageService _localStorage;
    private UserSession? _currentUser;

    public AuthService(HttpClient httpClient, ILocalStorageService localStorage)
    {
        _httpClient = httpClient;
        _localStorage = localStorage;
    }

    public UserSession? CurrentUser => _currentUser;
    public bool IsAuthenticated => _currentUser?.IsAuthenticated ?? false;

    public async Task<(bool success, string message)> LoginAsync(string email, string password, string userType)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("auth/login", new
            {
                email,
                password,
                userType
            });

            var result = await response.Content.ReadFromJsonAsync<ApiResponse<Dictionary<string, object>>>();

            if (result?.Success == true && result.Data != null)
            {
                // Handle JsonElement values properly
                long userId = 0;
                string userTypeValue = "";
                string nameValue = "";
                string tokenValue = "";

                if (result.Data.TryGetValue("userId", out var userIdObj))
                {
                    if (userIdObj is System.Text.Json.JsonElement userIdElement)
                        userId = userIdElement.GetInt64();
                    else
                        userId = Convert.ToInt64(userIdObj);
                }

                if (result.Data.TryGetValue("userType", out var userTypeObj))
                    userTypeValue = userTypeObj?.ToString() ?? "";

                if (result.Data.TryGetValue("name", out var nameObj))
                    nameValue = nameObj?.ToString() ?? "";

                if (result.Data.TryGetValue("token", out var tokenObj))
                    tokenValue = tokenObj?.ToString() ?? "";

                _currentUser = new UserSession
                {
                    UserId = userId,
                    UserType = userTypeValue,
                    Name = nameValue,
                    Token = tokenValue
                };

                await _localStorage.SetItemAsync("user", _currentUser);
                return (true, "Login successful");
            }

            return (false, result?.Message ?? "Login failed");
        }
        catch (Exception ex)
        {
            return (false, $"Error: {ex.Message}");
        }
    }

    public async Task<(bool success, string message)> RegisterAsync(
        string name, string email, string phone, string password, 
        string userType, string? specialization = null, string? role = null)
    {
        try
        {
            var response = await _httpClient.PostAsJsonAsync("auth/register", new
            {
                name,
                email,
                phone,
                password,
                userType,
                specialization = specialization ?? "",
                role = role ?? ""
            });

            var result = await response.Content.ReadFromJsonAsync<ApiResponse<object>>();
            return (result?.Success ?? false, result?.Message ?? "Registration failed");
        }
        catch (Exception ex)
        {
            return (false, $"Error: {ex.Message}");
        }
    }

    public async Task LogoutAsync()
    {
        _currentUser = null;
        await _localStorage.RemoveItemAsync("user");
    }

    public async Task InitializeAsync()
    {
        _currentUser = await _localStorage.GetItemAsync<UserSession>("user");
    }
}


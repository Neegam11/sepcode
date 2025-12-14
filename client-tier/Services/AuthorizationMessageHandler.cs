using System.Net.Http.Headers;
using Blazored.LocalStorage;
using ClinicClient.Models;

namespace ClinicClient.Services;

public class AuthorizationMessageHandler : DelegatingHandler
{
    private readonly ILocalStorageService _localStorage;

    public AuthorizationMessageHandler(ILocalStorageService localStorage)
    {
        _localStorage = localStorage;
    }

    protected override async Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request,
        CancellationToken cancellationToken)
    {
        var path = request.RequestUri?.PathAndQuery ?? "";
        if (!path.Contains("/auth/"))
        {
            try
            {
                var user = await _localStorage.GetItemAsync<UserSession>("user", cancellationToken);

                if (user != null && !string.IsNullOrEmpty(user.Token))
                {
                    request.Headers.Authorization =
                        new AuthenticationHeaderValue("Bearer", user.Token);
                }
            }
            catch { }
        }

        return await base.SendAsync(request, cancellationToken);
    }
}
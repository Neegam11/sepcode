using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using ClinicClient;
using ClinicClient.Services;
using Blazored.LocalStorage;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");


builder.Services.AddScoped(sp => new HttpClient 
{ 
    BaseAddress = new Uri("http://localhost:8080/api/") 
});


builder.Services.AddBlazoredLocalStorage();

//Register Services
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<AppointmentService>();
builder.Services.AddScoped<DoctorService>();
builder.Services.AddScoped<NotificationService>();
builder.Services.AddScoped<StaffService>();

await builder.Build().RunAsync();


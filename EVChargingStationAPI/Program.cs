// ========================================
// Program.cs
// ========================================
/*
 * Program.cs
 * Main entry point for the EV Charging Station API
 * Date: September 2025
 * Description: Configures and starts the web API server with all required services
 */

using DotNetEnv;
using EVChargingStationAPI.Models;
//using EVChargingStationAPI.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using MongoDB.Driver;
using System.Text;

// Load .env into process environment variables (do this BEFORE creating the builder so configuration picks them up)
Env.Load();


var builder = WebApplication.CreateBuilder(args);


// Ensure environment variables are available to IConfiguration (redundant in many hosts but safe)
builder.Configuration.AddEnvironmentVariables();

// Add MongoDB configuration
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("ConnectionStrings"));

builder.Services.AddSingleton<IMongoClient>(serviceProvider =>
{
    var connectionString = builder.Configuration.GetConnectionString("MongoDB");
    return new MongoClient(connectionString);
});

// Add services to the container
//builder.Services.AddScoped<IUserService, UserService>();
//builder.Services.AddScoped<IEVOwnerService, EVOwnerService>();
//builder.Services.AddScoped<IChargingStationService, ChargingStationService>();
//builder.Services.AddScoped<IBookingService, BookingService>();
//builder.Services.AddScoped<IQRService, QRService>();
//builder.Services.AddScoped<IAuthService, AuthService>();

// Add JWT authentication
var jwtSettings = builder.Configuration.GetSection("JWT");
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwtSettings["Issuer"],
            ValidAudience = jwtSettings["Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(jwtSettings["SecretKey"]))
        };
    });

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseCors("AllowAll");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
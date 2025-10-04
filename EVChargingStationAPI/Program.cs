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
using EVChargingStationAPI.Middleware;
using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using EVChargingStationAPI.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.RateLimiting;
using Microsoft.IdentityModel.Tokens;
using MongoDB.Driver;
using System.Text;
using System.Threading.RateLimiting;

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
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IEVOwnerService, EVOwnerService>();
builder.Services.AddScoped<IChargingStationService, ChargingStationService>();
builder.Services.AddScoped<IBookingService, BookingService>();
builder.Services.AddScoped<IQRService, QRService>();
builder.Services.AddScoped<IAuthService, AuthService>();

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

        // extract token from cookie named "accessToken"
        options.Events = new JwtBearerEvents
        {
            OnMessageReceived = context =>
            {
                if (context.Request.Cookies.TryGetValue("accessToken", out var token))
                {
                    context.Token = token;
                }
                return Task.CompletedTask;
            }
        };
    });

// Always enforce validation filter
builder.Services.AddControllers(options =>
{
    options.Filters.Add<ValidationFilter>();
})
.ConfigureApiBehaviorOptions(options =>
{
    // Disable automatic 400 response
    options.SuppressModelStateInvalidFilter = true;
})
.AddJsonOptions(opt => opt.JsonSerializerOptions.PropertyNameCaseInsensitive = true);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "EV Charging Station API",
        Version = "v1"
    });

    // Add JWT Authentication to Swagger
    options.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = Microsoft.OpenApi.Models.SecuritySchemeType.ApiKey,
        Scheme = "Bearer",
        BearerFormat = "JWT",
        In = Microsoft.OpenApi.Models.ParameterLocation.Header,
        Description = "Enter 'Bearer' [space] and then your token.\n\nExample: Bearer 12345abcdef"
    });

    options.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            new string[] {}
        }
    });

    // Automatically map ApiResponseDTO<T> so response body is visible
    options.MapType(typeof(ApiResponseDTO<>), () => new Microsoft.OpenApi.Models.OpenApiSchema
    {
        Type = "object",
        Properties =
        {
            ["success"] = new Microsoft.OpenApi.Models.OpenApiSchema { Type = "boolean" },
            ["message"] = new Microsoft.OpenApi.Models.OpenApiSchema { Type = "string" },
            ["data"] = new Microsoft.OpenApi.Models.OpenApiSchema { Type = "object", Nullable = true }
        }
    });
});

// Add CORS (strict - only allow frontend origin)
var frontendOrigin = builder.Configuration["Frontend__Origin"];
string[] allowedOrigins = frontendOrigin?.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries) ?? Array.Empty<string>();

builder.Services.AddCors(options =>
{
    options.AddPolicy("FrontendPolicy", policy =>
    {
        if (allowedOrigins.Length > 0)
        {
            policy.WithOrigins(allowedOrigins)
                  .AllowAnyMethod()
                  .AllowAnyHeader()
                  .AllowCredentials();
        }
    });
});


// Add rate limiting (configurable from .env)
var rateLimitWindow = int.TryParse(builder.Configuration["RateLimiting__WindowMinutes"], out var wm) ? wm : 1;
var rateLimitPermit = int.TryParse(builder.Configuration["RateLimiting__PermitLimit"], out var pl) ? pl : 100;

builder.Services.AddRateLimiter(options =>
{
    options.AddFixedWindowLimiter("FixedWindowPolicy", limiterOptions =>
    {
        limiterOptions.Window = TimeSpan.FromMinutes(rateLimitWindow);
        limiterOptions.PermitLimit = rateLimitPermit;
        limiterOptions.QueueProcessingOrder = QueueProcessingOrder.OldestFirst;
        limiterOptions.QueueLimit = 0;
    });

    // Generic error response when rate limit is exceeded
    options.OnRejected = async (context, token) =>
    {
        context.HttpContext.Response.StatusCode = StatusCodes.Status429TooManyRequests;
        context.HttpContext.Response.ContentType = "application/json";

        var response = new
        {
            Success = false,
            Message = "Rate limit exceeded. Please try again later."
        };

        await context.HttpContext.Response.WriteAsJsonAsync(response, cancellationToken: token);
    };
});

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
else
{
    app.UseGlobalExceptionHandler(); // safe wrapper
}


app.UseHttpsRedirection();
app.UseCors("FrontendPolicy");
app.UseRateLimiter();
app.UseAuthentication();
app.UseMiddleware<TokenRefreshMiddleware>();
app.UseAuthorization();
app.MapControllers();

app.Run();
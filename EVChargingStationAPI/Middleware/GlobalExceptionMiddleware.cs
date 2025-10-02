// ========================================
// Middleware/GlobalExceptionMiddleware.cs
// ========================================
/*
 * GlobalExceptionMiddleware.cs
 * Handle 500 status errors
 * Date: September 2025
 * Description: Avoid revealing too much info with server erros.
 */

using EVChargingStationAPI.Models.DTOs;

namespace EVChargingStationAPI.Middleware
{
    /// <summary>
    /// Middleware that converts server error's message
    /// </summary>
    public class GlobalExceptionMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILogger<GlobalExceptionMiddleware> _logger;

        /// <summary>
        /// Constructor to initialize the middleware
        /// </summary>
        /// <param name="next">The next middleware in the pipeline</param>
        /// <param name="logger">Provider for dependency injection</param>
        public GlobalExceptionMiddleware(RequestDelegate next, ILogger<GlobalExceptionMiddleware> logger)
        {
            _next = next;
            _logger = logger;
        }

        /// <summary>
        /// Main middleware execution method that does the conversion
        /// </summary>
        /// <param name="context">The HTTP context for the current request</param>
        public async Task InvokeAsync(HttpContext context)
        {
            try
            {
                await _next(context);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unhandled exception occurred");

                context.Response.ContentType = "application/json";
                context.Response.StatusCode = StatusCodes.Status500InternalServerError;

                var response = new ApiResponseDTO<string>
                {
                    Success = false,
                    Message = "An unexpected error occurred. Please try again later."
                };

                await context.Response.WriteAsJsonAsync(response);
            }
        }
    }

    /// <summary>
    /// Extension method for adding the <see cref="GlobalExceptionMiddleware"/> 
    /// into the application's request processing pipeline.
    /// </summary>
    /// <param name="builder">The application builder used to configure the middleware pipeline.</param>
    /// <returns>The updated <see cref="IApplicationBuilder"/> with the middleware registered.</returns>
    public static class GlobalExceptionMiddlewareExtensions
    {
        public static IApplicationBuilder UseGlobalExceptionHandler(this IApplicationBuilder builder)
        {
            return builder.UseMiddleware<GlobalExceptionMiddleware>();
        }
    }
}

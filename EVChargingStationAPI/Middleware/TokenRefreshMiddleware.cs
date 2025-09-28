// ========================================
// Middleware/TokenRefreshMiddleware.cs
// ========================================
/*
 * TokenRefreshMiddleware.cs
 * Automatic token refresh middleware
 * Date: September 2025
 * Description: Automatically refreshes JWT access tokens when they are about to expire
 *              using refresh tokens stored in HTTP-only cookies
 */

using System.IdentityModel.Tokens.Jwt;
using EVChargingStationAPI.Services;
using EVChargingStationAPI.Models.DTOs;
using Microsoft.AspNetCore.Http;

namespace EVChargingStationAPI.Middleware
{
    /// <summary>
    /// Middleware that automatically refreshes JWT access tokens when they are about to expire
    /// </summary>
    public class TokenRefreshMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly IServiceProvider _serviceProvider;

        /// <summary>
        /// Constructor to initialize the middleware with request delegate and service provider
        /// </summary>
        /// <param name="next">The next middleware in the pipeline</param>
        /// <param name="serviceProvider">Service provider for dependency injection</param>
        public TokenRefreshMiddleware(RequestDelegate next, IServiceProvider serviceProvider)
        {
            _next = next;
            _serviceProvider = serviceProvider;
        }

        /// <summary>
        /// Main middleware execution method that checks for expiring tokens and refreshes them automatically
        /// </summary>
        /// <param name="context">The HTTP context for the current request</param>
        public async Task InvokeAsync(HttpContext context)
        {
            // Extract the JWT token from the Authorization header
            var token = ExtractTokenFromHeader(context);

            if (!string.IsNullOrEmpty(token))
            {
                try
                {
                    // Validate and parse the JWT token
                    if (IsValidToken(token, out var jsonToken))
                    {
                        // Check if the token expires within the next 5 minutes
                        if (IsTokenNearExpiry(jsonToken))
                        {
                            // Attempt to refresh the token using the refresh token from cookies
                            await AttemptTokenRefresh(context);
                        }
                    }
                }
                catch (Exception ex)
                {
                    // Log the exception if needed (add logging here if you have a logger)
                    // For now, just continue with the request even if token refresh fails
                }
            }

            // Continue to the next middleware in the pipeline
            await _next(context);
        }

        /// <summary>
        /// Extracts the JWT token from the Authorization header
        /// </summary>
        /// <param name="context">The HTTP context</param>
        /// <returns>The JWT token string or null if not found</returns>
        private string? ExtractTokenFromHeader(HttpContext context)
        {
            var authHeader = context.Request.Headers["Authorization"].ToString();
            return authHeader.StartsWith("Bearer ") ? authHeader.Replace("Bearer ", "") : null;
        }

        /// <summary>
        /// Validates if the token is a valid JWT token and can be read
        /// </summary>
        /// <param name="token">The JWT token string</param>
        /// <param name="jsonToken">Output parameter for the parsed JWT token</param>
        /// <returns>True if the token is valid, false otherwise</returns>
        private bool IsValidToken(string token, out JwtSecurityToken? jsonToken)
        {
            jsonToken = null;
            var tokenHandler = new JwtSecurityTokenHandler();

            if (!tokenHandler.CanReadToken(token))
                return false;

            jsonToken = tokenHandler.ReadJwtToken(token);
            return true;
        }

        /// <summary>
        /// Checks if the JWT token expires within the next 5 minutes
        /// </summary>
        /// <param name="jsonToken">The parsed JWT token</param>
        /// <returns>True if the token expires within 5 minutes, false otherwise</returns>
        private bool IsTokenNearExpiry(JwtSecurityToken jsonToken)
        {
            return jsonToken.ValidTo <= DateTime.UtcNow.AddMinutes(5);
        }

        /// <summary>
        /// Attempts to refresh the access token using the refresh token from cookies
        /// </summary>
        /// <param name="context">The HTTP context</param>
        private async Task AttemptTokenRefresh(HttpContext context)
        {
            // Get the refresh token from the HTTP-only cookie
            var refreshToken = context.Request.Cookies["refreshToken"];

            if (string.IsNullOrEmpty(refreshToken))
                return;

            // Create a scope to resolve the AuthService dependency
            using var scope = _serviceProvider.CreateScope();
            var authService = scope.ServiceProvider.GetRequiredService<IAuthService>();

            // Attempt to refresh the token
            var refreshResult = await authService.RefreshTokenAsync(new RefreshTokenRequestDTO
            {
                RefreshToken = refreshToken
            });

            if (refreshResult.Success && refreshResult.Data != null)
            {
                // Set the new refresh token as an HTTP-only cookie
                SetRefreshTokenCookie(context, refreshResult.Data.RefreshToken, refreshResult.Data.RefreshTokenExpiresAt);

                // Send the new access token back to the client via response header
                context.Response.Headers.Add("X-New-Access-Token", refreshResult.Data.AccessToken);
            }
        }

        /// <summary>
        /// Sets the refresh token as an HTTP-only cookie with security settings
        /// </summary>
        /// <param name="context">The HTTP context</param>
        /// <param name="refreshToken">The refresh token to store</param>
        /// <param name="expiresAt">When the refresh token expires</param>
        private void SetRefreshTokenCookie(HttpContext context, string refreshToken, DateTime expiresAt)
        {
            var cookieOptions = new CookieOptions
            {
                HttpOnly = true,           // Cannot be accessed by JavaScript (XSS protection)
                Secure = true,             // Only send over HTTPS
                SameSite = SameSiteMode.Strict,  // CSRF protection
                Expires = expiresAt        // Set cookie expiration to match token expiration
            };

            context.Response.Cookies.Append("refreshToken", refreshToken, cookieOptions);
        }
    }
}
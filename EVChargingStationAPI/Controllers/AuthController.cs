// ========================================
// Controllers/AuthController.cs
// ========================================
/*
 * AuthController.cs
 * Authentication controller for user login
 * Date: September 2025
 * Description: Handles authentication endpoints for web users and EV owners
 */

using EVChargingStationAPI.Models.DTOs;
using EVChargingStationAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EVChargingStationAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;

        /// <summary>
        /// Constructor to initialize authentication controller
        /// </summary>
        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        /// <summary>
        /// Authenticates web application users (BackOffice and Station Operators)
        /// </summary>
        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequestDTO loginRequest)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                var result = await _authService.LoginUserAsync(loginRequest);

                if (result.Success)
                {
                    // Set refresh token as HTTP-only cookie
                    var cookieOptions = new CookieOptions
                    {
                        HttpOnly = true,
                        Secure = true, // Only send over HTTPS
                        SameSite = SameSiteMode.Strict,
                        Expires = result.Data.RefreshTokenExpiresAt
                    };

                    Response.Cookies.Append("refreshToken", result.Data.RefreshToken, cookieOptions);

                    // Remove refresh token from response body for security
                    result.Data.RefreshToken = string.Empty;

                    return Ok(result);
                }

                return Unauthorized(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Authenticates EV owners using NIC and password
        /// </summary>
        [HttpPost("login/evowner")]
        public async Task<IActionResult> LoginEVOwner([FromBody] EVOwnerLoginRequestDTO loginRequest)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                var result = await _authService.LoginEVOwnerAsync(loginRequest);

                if (result.Success)
                {
                    // Set refresh token as HTTP-only cookie
                    var cookieOptions = new CookieOptions
                    {
                        HttpOnly = true,
                        Secure = true, // Only send over HTTPS
                        SameSite = SameSiteMode.Strict,
                        Expires = result.Data.RefreshTokenExpiresAt
                    };

                    Response.Cookies.Append("refreshToken", result.Data.RefreshToken, cookieOptions);

                    // Remove refresh token from response body for security
                    result.Data.RefreshToken = string.Empty;

                    return Ok(result);
                }

                return Unauthorized(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Refreshes JWT access token using a valid refresh token
        /// </summary>
        [HttpPost("refresh")]
        public async Task<IActionResult> RefreshToken()
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                // Get refresh token from cookie
                var refreshToken = Request.Cookies["refreshToken"];

                if (string.IsNullOrEmpty(refreshToken))
                {
                    return Unauthorized(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "No refresh token found"
                    });
                }

                var refreshTokenRequest = new RefreshTokenRequestDTO
                {
                    RefreshToken = refreshToken
                };

                var result = await _authService.RefreshTokenAsync(refreshTokenRequest);
                if (result.Success)
                {
                    // Set new refresh token as HTTP-only cookie
                    var cookieOptions = new CookieOptions
                    {
                        HttpOnly = true,
                        Secure = true,
                        SameSite = SameSiteMode.Strict,
                        Expires = result.Data.RefreshTokenExpiresAt
                    };

                    Response.Cookies.Append("refreshToken", result.Data.RefreshToken, cookieOptions);

                    // Remove refresh token from response body
                    result.Data.RefreshToken = string.Empty;

                    return Ok(result);
                }
                return Unauthorized(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Logs out a user by invalidating their refresh token
        /// </summary>
        [HttpPost("logout")]
        [Authorize]
        public async Task<IActionResult> Logout()
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                var refreshToken = Request.Cookies["refreshToken"];

                if (!string.IsNullOrEmpty(refreshToken))
                {
                    var logoutRequest = new LogoutRequestDTO
                    {
                        RefreshToken = refreshToken
                    };

                    await _authService.LogoutAsync(logoutRequest);
                }

                // Clear the refresh token cookie
                Response.Cookies.Delete("refreshToken");

                return Ok(new ApiResponseDTO<object>
                {
                    Success = true,
                    Message = "Logged out successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }
    }
}
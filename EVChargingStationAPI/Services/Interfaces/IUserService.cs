// ========================================
// Services/Interfaces/IUserService.cs
// ========================================
/*
 * IUserService.cs
 * User service interface
 * Date: September 2025
 * Description: Defines user management service contract
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;

namespace EVChargingStationAPI.Services
{
    public interface IUserService
    {
        Task<ApiResponseDTO<User>> CreateUserAsync(CreateUserDTO createUserDto);
        Task<ApiResponseDTO<List<User>>> GetAllUsersAsync();
        Task<ApiResponseDTO<User>> GetUserByIdAsync(string id);
        Task<ApiResponseDTO<User>> UpdateUserAsync(string id, User user);
        Task<ApiResponseDTO<bool>> DeleteUserAsync(string id);
        Task<ApiResponseDTO<bool>> ActivateDeactivateUserAsync(string id, bool isActive);
    }
}
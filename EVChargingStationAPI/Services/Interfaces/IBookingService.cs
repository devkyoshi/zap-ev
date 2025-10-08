// ========================================
// Services/Interfaces/IBookingService.cs
// ========================================
/*
 * IBookingService.cs
 * Booking service interface
 * Author: Your Group
 * Date: September 2025
 * Description: Defines booking management service contract
 */

using EVChargingStationAPI.Models.DTOs;

namespace EVChargingStationAPI.Services
{
    public interface IBookingService
    {
        Task<ApiResponseDTO<BookingResponseDTO>> CreateBookingAsync(string evOwnerNIC, CreateBookingDTO createBookingDto);
        Task<ApiResponseDTO<List<BookingResponseDTO>>> GetAllBookingsAsync(string userId);
        Task<ApiResponseDTO<List<BookingResponseDTO>>> GetAllBookingsAsync2();

        Task<ApiResponseDTO<List<BookingResponseDTO>>> GetBookingsByEVOwnerAsync(string evOwnerNIC);
        Task<ApiResponseDTO<BookingResponseDTO>> GetBookingByIdAsync(string id);
        Task<ApiResponseDTO<BookingResponseDTO>> UpdateBookingAsync(string id, string evOwnerNIC, UpdateBookingDTO updateBookingDto);
        Task<ApiResponseDTO<bool>> CancelBookingAsync(string id, string evOwnerNIC);
        Task<ApiResponseDTO<BookingResponseDTO>> ApproveBookingAsync(string id, string userId);
        Task<ApiResponseDTO<BookingResponseDTO>> StartBookingAsync(string id, string userId);
        Task<ApiResponseDTO<BookingResponseDTO>> CompleteBookingAsync(string id, string userId);
        Task<ApiResponseDTO<List<BookingResponseDTO>>> GetUpcomingBookingsAsync(string evOwnerNIC);
        Task<ApiResponseDTO<List<BookingResponseDTO>>> GetBookingHistoryAsync(string evOwnerNIC);
        Task<ApiResponseDTO<List<SessionHistoryDTO>>> GetSessionHistoryAsync();
    }
}
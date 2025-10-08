// ========================================
// Services/Interfaces/IQRService.cs
// ========================================
/*
 * IQRService.cs
 * QR Code service interface
 * Date: September 2025
 * Description: Defines QR code generation and verification service contract
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;

namespace EVChargingStationAPI.Services
{
    public interface IQRService
    {
        string GenerateQRCode(Booking booking);
        Task<ApiResponseDTO<Booking>> VerifyQRCodeAsync(string qrCode);
        bool ValidateQRCode(string qrCode);
    }
}
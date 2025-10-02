// ========================================
// Services/QRService.cs
// ========================================
/*
 * QRService.cs
 * QR Code service implementation
 * Date: September 2025
 * Description: Handles QR code generation and verification for bookings
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using MongoDB.Driver;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace EVChargingStationAPI.Services
{
    public class QRService : IQRService
    {
        private readonly IMongoCollection<Booking> _bookings;
        private readonly string _secretKey;

        /// <summary>
        /// Constructor to initialize QR service with database collection and configuration
        /// </summary>
        public QRService(IMongoClient mongoClient, IConfiguration configuration)
        {
            var database = mongoClient.GetDatabase("EVChargingStationDB");
            _bookings = database.GetCollection<Booking>("Bookings");
            _secretKey = configuration["JWT:SecretKey"] ?? "DefaultSecretKey";
        }

        /// <summary>
        /// Generates a secure QR code for a booking
        /// </summary>
        public string GenerateQRCode(Booking booking)
        {
            try
            {
                var qrData = new QRData
                {
                    BookingId = booking.Id,
                    EVOwnerNIC = booking.EVOwnerNIC,
                    ChargingStationId = booking.ChargingStationId,
                    ReservationDateTime = booking.ReservationDateTime.ToString("yyyy-MM-ddTHH:mm:ssZ"),
                    Timestamp = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
                };

                var jsonData = JsonSerializer.Serialize(qrData);
                var encodedData = Convert.ToBase64String(Encoding.UTF8.GetBytes(jsonData));

                // Create a hash to verify integrity
                var hash = GenerateHash($"{encodedData}{_secretKey}");

                return $"{encodedData}.{hash}";
            }
            catch (Exception ex)
            {
                throw new Exception("Failed to generate QR code", ex);
            }
        }

        /// <summary>
        /// Verifies QR code and retrieves associated booking
        /// </summary>
        public async Task<ApiResponseDTO<Booking>> VerifyQRCodeAsync(string qrCode)
        {
            try
            {
                if (!ValidateQRCode(qrCode))
                {
                    return new ApiResponseDTO<Booking>
                    {
                        Success = false,
                        Message = "Invalid QR code format"
                    };
                }

                var parts = qrCode.Split('.');
                var encodedData = parts[0];
                var hash = parts[1];

                // Verify hash integrity
                var expectedHash = GenerateHash($"{encodedData}{_secretKey}");
                if (hash != expectedHash)
                {
                    return new ApiResponseDTO<Booking>
                    {
                        Success = false,
                        Message = "QR code has been tampered with"
                    };
                }

                var jsonData = Encoding.UTF8.GetString(Convert.FromBase64String(encodedData));
                var qrData = JsonSerializer.Deserialize<QRData>(jsonData);

                if (qrData == null)
                {
                    return new ApiResponseDTO<Booking>
                    {
                        Success = false,
                        Message = "Invalid QR code data"
                    };
                }

                // Check if QR code is not too old (24 hours max)
                if (DateTime.TryParse(qrData.Timestamp, out var timestamp))
                {
                    if (DateTime.UtcNow.Subtract(timestamp).TotalHours > 24)
                    {
                        return new ApiResponseDTO<Booking>
                        {
                            Success = false,
                            Message = "QR code has expired"
                        };
                    }
                }

                var booking = await _bookings.Find(b => b.Id == qrData.BookingId).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<Booking>
                    {
                        Success = false,
                        Message = "Booking not found"
                    };
                }

                if (booking.EVOwnerNIC != qrData.EVOwnerNIC ||
                    booking.ChargingStationId != qrData.ChargingStationId)
                {
                    return new ApiResponseDTO<Booking>
                    {
                        Success = false,
                        Message = "QR code data mismatch"
                    };
                }

                return new ApiResponseDTO<Booking>
                {
                    Success = true,
                    Message = "QR code verified successfully",
                    Data = booking
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<Booking>
                {
                    Success = false,
                    Message = "An error occurred while verifying QR code"
                };
            }
        }

        /// <summary>
        /// Validates QR code format
        /// </summary>
        public bool ValidateQRCode(string qrCode)
        {
            if (string.IsNullOrWhiteSpace(qrCode))
                return false;

            var parts = qrCode.Split('.');
            return parts.Length == 2 && !string.IsNullOrWhiteSpace(parts[0]) && !string.IsNullOrWhiteSpace(parts[1]);
        }

        /// <summary>
        /// Generates SHA256 hash for data integrity
        /// </summary>
        private static string GenerateHash(string input)
        {
            using var sha256 = SHA256.Create();
            var hashBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(input));
            return Convert.ToBase64String(hashBytes);
        }

        /// <summary>
        /// Internal class for QR data structure
        /// </summary>
        private class QRData
        {
            public string BookingId { get; set; } = string.Empty;
            public string EVOwnerNIC { get; set; } = string.Empty;
            public string ChargingStationId { get; set; } = string.Empty;
            public string ReservationDateTime { get; set; } = string.Empty;
            public string Timestamp { get; set; } = string.Empty;
        }
    }
}
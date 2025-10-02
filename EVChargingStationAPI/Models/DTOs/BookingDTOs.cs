// ========================================
// Models/DTOs/BookingDTOs.cs
// ========================================
/*
 * BookingDTOs.cs
 * Data Transfer Objects for booking operations
 * Date: September 2025
 * Description: Contains DTOs for booking creation, updates, and responses
 */

using System.ComponentModel.DataAnnotations;

namespace EVChargingStationAPI.Models.DTOs
{
    public class CreateBookingDTO
    {
        [Required]
        public string ChargingStationId { get; set; } = string.Empty;

        [Required]
        public DateTime ReservationDateTime { get; set; }

        [Required]
        [Range(30, 480)] // 30 minutes to 8 hours
        public int DurationMinutes { get; set; }

        public string Notes { get; set; } = string.Empty;
    }

    public class UpdateBookingDTO
    {
        public DateTime? ReservationDateTime { get; set; }
        public int? DurationMinutes { get; set; }
        public string Notes { get; set; } = string.Empty;
    }

    public class BookingResponseDTO
    {
        public string Id { get; set; } = string.Empty;
        public string EVOwnerNIC { get; set; } = string.Empty;
        public string ChargingStationName { get; set; } = string.Empty;
        public DateTime ReservationDateTime { get; set; }
        public int DurationMinutes { get; set; }
        public BookingStatus Status { get; set; }
        public decimal TotalAmount { get; set; }
        public string QRCode { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
    }

    public class QRVerificationDTO
    {
        [Required]
        public string QRCode { get; set; } = string.Empty;
    }

    public class BookingConfirmationDTO
    {
        [Required]
        public string BookingId { get; set; } = string.Empty;

        [Required]
        public string Action { get; set; } = string.Empty; // "start", "complete"
    }
}
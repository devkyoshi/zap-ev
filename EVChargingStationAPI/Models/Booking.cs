// ========================================
// Models/Booking.cs
// ========================================
/*
 * Booking.cs
 * Booking model for charging reservations
 * Date: September 2025
 * Description: Represents charging station bookings by EV owners
 */

using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingStationAPI.Models
{
    public class Booking : BaseEntity
    {
        [BsonElement("evOwnerNIC")]
        public string EVOwnerNIC { get; set; } = string.Empty;

        [BsonElement("chargingStationId")]
        public string ChargingStationId { get; set; } = string.Empty;

        [BsonElement("reservationDateTime")]
        public DateTime ReservationDateTime { get; set; }

        [BsonElement("duration")]
        public int DurationMinutes { get; set; }

        [BsonElement("status")]
        public BookingStatus Status { get; set; } = BookingStatus.Pending;

        [BsonElement("totalAmount")]
        public decimal TotalAmount { get; set; }

        [BsonElement("qrCode")]
        public string QRCode { get; set; } = string.Empty;

        [BsonElement("actualStartTime")]
        public DateTime? ActualStartTime { get; set; }

        [BsonElement("actualEndTime")]
        public DateTime? ActualEndTime { get; set; }

        [BsonElement("notes")]
        public string Notes { get; set; } = string.Empty;
    }

    public enum BookingStatus
    {
        Pending = 1,
        Approved = 2,
        InProgress = 3,
        Completed = 4,
        Cancelled = 5,
        NoShow = 6
    }
}
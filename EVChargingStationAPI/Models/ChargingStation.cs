// ========================================
// Models/ChargingStation.cs
// ========================================
/*
 * ChargingStation.cs
 * Charging Station model
 * Date: September 2025
 * Description: Represents charging stations with location and availability
 */

using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingStationAPI.Models
{
    public class ChargingStation : BaseEntity
    {
        [BsonElement("name")]
        public string Name { get; set; } = string.Empty;

        [BsonElement("location")]
        public Location Location { get; set; } = new();

        [BsonElement("type")]
        public ChargingType Type { get; set; }

        [BsonElement("totalSlots")]
        public int TotalSlots { get; set; }

        [BsonElement("availableSlots")]
        public int AvailableSlots { get; set; }

        [BsonElement("pricePerHour")]
        public decimal PricePerHour { get; set; }

        [BsonElement("operatingHours")]
        public OperatingHours OperatingHours { get; set; } = new();

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("amenities")]
        public List<string> Amenities { get; set; } = new();
    }

    public class Location
    {
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string Address { get; set; } = string.Empty;
        public string City { get; set; } = string.Empty;
        public string Province { get; set; } = string.Empty;
    }

    public class OperatingHours
    {
        public TimeSpan OpenTime { get; set; }
        public TimeSpan CloseTime { get; set; }
        public List<DayOfWeek> OperatingDays { get; set; } = new();
    }

    public enum ChargingType
    {
        AC = 1,
        DC = 2
    }
}
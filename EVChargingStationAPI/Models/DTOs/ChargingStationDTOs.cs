// ========================================
// Models/DTOs/ChargingStationDTOs.cs
// ========================================
/*
 * ChargingStationDTOs.cs
 * Data Transfer Objects for charging station operations
 * Date: September 2025
 * Description: Contains DTOs for charging station management
 */

using System.ComponentModel.DataAnnotations;

namespace EVChargingStationAPI.Models.DTOs
{
    public class CreateChargingStationDTO
    {
        [Required]
        public string Name { get; set; } = string.Empty;

        [Required]
        public LocationDTO Location { get; set; } = new();

        [Required]
        public ChargingType Type { get; set; }

        [Required]
        [Range(1, 50)]
        public int TotalSlots { get; set; }

        [Required]
        [Range(0.01, 1000)]
        public decimal PricePerHour { get; set; }

        public OperatingHoursDTO OperatingHours { get; set; } = new();
        public List<string> Amenities { get; set; } = new();
    }

    public class UpdateChargingStationDTO
    {
        public string Name { get; set; } = string.Empty;
        public LocationDTO Location { get; set; } = new();
        public int? TotalSlots { get; set; }
        public decimal? PricePerHour { get; set; }
        public OperatingHoursDTO OperatingHours { get; set; } = new();
        public List<string> Amenities { get; set; } = new();
    }

    public class LocationDTO
    {
        [Required]
        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Required]
        [Range(-180, 180)]
        public double Longitude { get; set; }

        [Required]
        public string Address { get; set; } = string.Empty;

        public string City { get; set; } = string.Empty;
        public string Province { get; set; } = string.Empty;
    }

    public class OperatingHoursDTO
    {
        public TimeSpan OpenTime { get; set; }
        public TimeSpan CloseTime { get; set; }
        public List<DayOfWeek> OperatingDays { get; set; } = new(); //Sunday (0) to Saturday (6).
    }

    public class NearbyStationsRequestDTO
    {
        [Required]
        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Required]
        [Range(-180, 180)]
        public double Longitude { get; set; }

        [Range(1, 50)]
        public double RadiusKm { get; set; } = 10;
    }
}

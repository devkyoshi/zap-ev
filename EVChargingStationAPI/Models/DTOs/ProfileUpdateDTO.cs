// ========================================
// Models/DTOs/ProfileUpdateDTO.cs
// ========================================
/*
 * ProfileUpdateDTO.cs
 * Data Transfer Object for profile update operations
 * Date: October 2025
 * Description: DTO specifically for profile updates from mobile app
 */

using System.ComponentModel.DataAnnotations;

namespace EVChargingStationAPI.Models.DTOs
{
    /// <summary>
    /// DTO for updating EV Owner profile information
    /// This DTO excludes sensitive fields like password, active status, etc.
    /// </summary>
    public class ProfileUpdateDTO
    {
        [Required]
        [StringLength(100)]
        public string FirstName { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string LastName { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        [StringLength(200)]
        public string Email { get; set; } = string.Empty;

        [Required]
        [Phone]
        [StringLength(20)]
        public string PhoneNumber { get; set; } = string.Empty;

        /// <summary>
        /// Vehicle details associated with the EV Owner
        /// </summary>
        public List<VehicleDetail> VehicleDetails { get; set; } = new();
    }
}
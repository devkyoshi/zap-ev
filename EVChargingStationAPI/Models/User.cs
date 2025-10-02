// ========================================
// Models/User.cs
// ========================================
/*
 * User.cs
 * User model for web application users
 * Date: September 2025
 * Description: Represents backoffice and station operator users
 */

using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingStationAPI.Models
{
    public class User : BaseEntity
    {
        [BsonElement("username")]
        public string Username { get; set; } = string.Empty;

        [BsonElement("email")]
        public string Email { get; set; } = string.Empty;

        [BsonElement("passwordHash")]
        public string PasswordHash { get; set; } = string.Empty;

        [BsonElement("role")]
        public UserRole Role { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("lastLogin")]
        public DateTime? LastLogin { get; set; }
    }

    public enum UserRole
    {
        BackOffice = 1,
        StationOperator = 2
    }
}
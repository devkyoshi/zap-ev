// ========================================
// Services/UserService.cs
// ========================================
/*
 * UserService.cs
 * User service implementation
 * Date: September 2025
 * Description: Handles user management operations for web application users
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using MongoDB.Driver;

namespace EVChargingStationAPI.Services
{
    public class UserService : IUserService
    {
        private readonly IMongoCollection<User> _users;
        private readonly IAuthService _authService;

        /// <summary>
        /// Constructor to initialize user service with database collection
        /// </summary>
        public UserService(IMongoClient mongoClient, IAuthService authService)
        {
            var database = mongoClient.GetDatabase("EVChargingStationDB");
            _users = database.GetCollection<User>("Users");
            _authService = authService;
        }

        /// <summary>
        /// Creates a new web application user
        /// </summary>
        public async Task<ApiResponseDTO<User>> CreateUserAsync(CreateUserDTO createUserDto)
        {
            try
            {
                // Check if username already exists
                var existingUser = await _users.Find(u => u.Username == createUserDto.Username).FirstOrDefaultAsync();
                if (existingUser != null)
                {
                    return new ApiResponseDTO<User>
                    {
                        Success = false,
                        Message = "Username already exists"
                    };
                }

                // Check if email already exists
                var existingEmail = await _users.Find(u => u.Email == createUserDto.Email).FirstOrDefaultAsync();
                if (existingEmail != null)
                {
                    return new ApiResponseDTO<User>
                    {
                        Success = false,
                        Message = "Email already exists"
                    };
                }

                var (isValid, message) = PasswordValidator.Validate(createUserDto.Password);
                if (!isValid)
                {
                    return new ApiResponseDTO<User>
                    {
                        Success = false,
                        Message = message
                    };
                }

                var user = new User
                {
                    Username = createUserDto.Username,
                    Email = createUserDto.Email,
                    PasswordHash = _authService.HashPassword(createUserDto.Password),
                    Role = createUserDto.Role,
                    IsActive = true
                };

                await _users.InsertOneAsync(user);

                // Remove password hash from response
                user.PasswordHash = string.Empty;

                return new ApiResponseDTO<User>
                {
                    Success = true,
                    Message = "User created successfully",
                    Data = user
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<User>
                {
                    Success = false,
                    Message = "An error occurred while creating the user"
                };
            }
        }

        /// <summary>
        /// Retrieves all users from the system
        /// </summary>
        public async Task<ApiResponseDTO<List<User>>> GetAllUsersAsync()
        {
            try
            {
                var users = await _users.Find(_ => true).ToListAsync();

                // Remove password hashes from response
                foreach (var user in users)
                {
                    user.PasswordHash = string.Empty;
                }

                return new ApiResponseDTO<List<User>>
                {
                    Success = true,
                    Message = "Users retrieved successfully",
                    Data = users
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<User>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving users"
                };
            }
        }

        /// <summary>
        /// Retrieves a user by their ID
        /// </summary>
        public async Task<ApiResponseDTO<User>> GetUserByIdAsync(string id)
        {
            try
            {
                var user = await _users.Find(u => u.Id == id).FirstOrDefaultAsync();

                if (user == null)
                {
                    return new ApiResponseDTO<User>
                    {
                        Success = false,
                        Message = "User not found"
                    };
                }

                user.PasswordHash = string.Empty;

                return new ApiResponseDTO<User>
                {
                    Success = true,
                    Message = "User retrieved successfully",
                    Data = user
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<User>
                {
                    Success = false,
                    Message = "An error occurred while retrieving the user"
                };
            }
        }

        /// <summary>
        /// Updates an existing user
        /// </summary>
        public async Task<ApiResponseDTO<User>> UpdateUserAsync(string id, User user)
        {
            try
            {
                var existingUser = await _users.Find(u => u.Id == id).FirstOrDefaultAsync();
                if (existingUser == null)
                {
                    return new ApiResponseDTO<User>
                    {
                        Success = false,
                        Message = "User not found"
                    };
                }

                user.Id = id;
                user.UpdatedAt = DateTime.UtcNow;
                user.CreatedAt = existingUser.CreatedAt;

                var result = await _users.ReplaceOneAsync(u => u.Id == id, user);

                if (result.ModifiedCount > 0)
                {
                    user.PasswordHash = string.Empty;
                    return new ApiResponseDTO<User>
                    {
                        Success = true,
                        Message = "User updated successfully",
                        Data = user
                    };
                }

                return new ApiResponseDTO<User>
                {
                    Success = false,
                    Message = "Failed to update user"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<User>
                {
                    Success = false,
                    Message = "An error occurred while updating the user"
                };
            }
        }

        /// <summary>
        /// Deletes a user from the system
        /// </summary>
        public async Task<ApiResponseDTO<bool>> DeleteUserAsync(string id)
        {
            try
            {
                var result = await _users.DeleteOneAsync(u => u.Id == id);

                if (result.DeletedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "User deleted successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "User not found"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while deleting the user"
                };
            }
        }

        /// <summary>
        /// Activates or deactivates a user account
        /// </summary>
        public async Task<ApiResponseDTO<bool>> ActivateDeactivateUserAsync(string id, bool isActive)
        {
            try
            {
                var update = Builders<User>.Update
                    .Set(u => u.IsActive, isActive)
                    .Set(u => u.UpdatedAt, DateTime.UtcNow);

                var result = await _users.UpdateOneAsync(u => u.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = $"User {(isActive ? "activated" : "deactivated")} successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "User not found"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while updating user status"
                };
            }
        }

        /// <summary>
        /// Gets all unassigned station operators
        /// </summary>
        public async Task<ApiResponseDTO<List<User>>> GetUnassignedStationOperatorsAsync()
        {
            try
            {
                var unassignedOperators = await _users.Find(u =>
                    u.Role == UserRole.StationOperator &&
                    u.IsActive &&
                    (u.ChargingStationIds == null || !u.ChargingStationIds.Any())
                ).ToListAsync();

                // Remove password hashes from response
                foreach (var user in unassignedOperators)
                {
                    user.PasswordHash = string.Empty;
                }

                return new ApiResponseDTO<List<User>>
                {
                    Success = true,
                    Message = "Unassigned station operators retrieved successfully",
                    Data = unassignedOperators
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<User>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving unassigned station operators"
                };
            }
        }
    }
}

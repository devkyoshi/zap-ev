// ========================================
// Services/ChargingStationService.cs
// ========================================
/*
 * ChargingStationService.cs
 * Charging Station service implementation
 * Date: September 2025
 * Description: Handles charging station management and location-based operations
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using MongoDB.Driver;

namespace EVChargingStationAPI.Services
{
    public class ChargingStationService : IChargingStationService
    {
        private readonly IMongoCollection<ChargingStation> _chargingStations;
        private readonly IMongoCollection<Booking> _bookings;
        private readonly IMongoDatabase _database;

        /// <summary>
        /// Constructor to initialize charging station service with database collections
        /// </summary>
        public ChargingStationService(IMongoClient mongoClient)
        {
            _database = mongoClient.GetDatabase("EVChargingStationDB");
            _chargingStations = _database.GetCollection<ChargingStation>("ChargingStations");
            _bookings = _database.GetCollection<Booking>("Bookings");
        }

        /// <summary>
        /// Creates a new charging station
        /// </summary>
        public async Task<ApiResponseDTO<ChargingStation>> CreateChargingStationAsync(CreateChargingStationDTO createStationDto, string backOfficeUserId)
        {
            try
            {
                var chargingStation = new ChargingStation
                {
                    Name = createStationDto.Name,
                    Location = new Location
                    {
                        Latitude = createStationDto.Location.Latitude,
                        Longitude = createStationDto.Location.Longitude,
                        Address = createStationDto.Location.Address,
                        City = createStationDto.Location.City,
                        Province = createStationDto.Location.Province
                    },
                    Type = createStationDto.Type,
                    TotalSlots = createStationDto.TotalSlots,
                    AvailableSlots = createStationDto.TotalSlots, // Initially all slots are available
                    PricePerHour = createStationDto.PricePerHour,
                    OperatingHours = new OperatingHours
                    {
                        OpenTime = createStationDto.OperatingHours.OpenTime,
                        CloseTime = createStationDto.OperatingHours.CloseTime,
                        OperatingDays = createStationDto.OperatingHours.OperatingDays
                    },
                    Amenities = createStationDto.Amenities,
                    IsActive = true
                };

                await _chargingStations.InsertOneAsync(chargingStation);

                // Update the BackOffice user with the charging station ID
                var userCollection = _database.GetCollection<User>("Users");
                var userUpdate = Builders<User>.Update.Set(u => u.ChargingStationId, chargingStation.Id);
                await userCollection.UpdateOneAsync(u => u.Id == backOfficeUserId, userUpdate);

                return new ApiResponseDTO<ChargingStation>
                {
                    Success = true,
                    Message = "Charging station created successfully",
                    Data = chargingStation
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<ChargingStation>
                {
                    Success = false,
                    Message = "An error occurred while creating the charging station"
                };
            }
        }

        /// <summary>
        /// Retrieves all charging stations from the system
        /// </summary>
        public async Task<ApiResponseDTO<List<ChargingStation>>> GetAllChargingStationsAsync()
        {
            try
            {
                var chargingStations = await _chargingStations.Find(_ => true).ToListAsync();

                return new ApiResponseDTO<List<ChargingStation>>
                {
                    Success = true,
                    Message = "Charging stations retrieved successfully",
                    Data = chargingStations
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<ChargingStation>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving charging stations"
                };
            }
        }

        /// <summary>
        /// Retrieves a charging station by its ID
        /// </summary>
        public async Task<ApiResponseDTO<ChargingStation>> GetChargingStationByIdAsync(string id)
        {
            try
            {
                var chargingStation = await _chargingStations.Find(s => s.Id == id).FirstOrDefaultAsync();

                if (chargingStation == null)
                {
                    return new ApiResponseDTO<ChargingStation>
                    {
                        Success = false,
                        Message = "Charging station not found"
                    };
                }

                return new ApiResponseDTO<ChargingStation>
                {
                    Success = true,
                    Message = "Charging station retrieved successfully",
                    Data = chargingStation
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<ChargingStation>
                {
                    Success = false,
                    Message = "An error occurred while retrieving the charging station"
                };
            }
        }

        /// <summary>
        /// Updates an existing charging station
        /// </summary>
        public async Task<ApiResponseDTO<ChargingStation>> UpdateChargingStationAsync(string id, UpdateChargingStationDTO updateStationDto, string userId)
        {
            try
            {
                // Check if user has permission to update the station
                if (!await CanUserAccessStation(userId, id))
                {
                    return new ApiResponseDTO<ChargingStation>
                    {
                        Success = false,
                        Message = "You don't have permission to update this charging station"
                    };
                }

                var existingStation = await _chargingStations.Find(s => s.Id == id).FirstOrDefaultAsync();
                if (existingStation == null)
                {
                    return new ApiResponseDTO<ChargingStation>
                    {
                        Success = false,
                        Message = "Charging station not found"
                    };
                }

                var updateBuilder = Builders<ChargingStation>.Update.Set(s => s.UpdatedAt, DateTime.UtcNow);

                if (!string.IsNullOrEmpty(updateStationDto.Name))
                    updateBuilder = updateBuilder.Set(s => s.Name, updateStationDto.Name);

                if (updateStationDto.Location != null && updateStationDto.Location.Latitude != 0 && updateStationDto.Location.Longitude != 0)
                {
                    updateBuilder = updateBuilder.Set(s => s.Location, new Location
                    {
                        Latitude = updateStationDto.Location.Latitude,
                        Longitude = updateStationDto.Location.Longitude,
                        Address = updateStationDto.Location.Address,
                        City = updateStationDto.Location.City,
                        Province = updateStationDto.Location.Province
                    });
                }

                if (updateStationDto.TotalSlots.HasValue)
                {
                    updateBuilder = updateBuilder.Set(s => s.TotalSlots, updateStationDto.TotalSlots.Value);
                    // Update available slots proportionally
                    var ratio = (double)existingStation.AvailableSlots / existingStation.TotalSlots;
                    var newAvailableSlots = (int)(updateStationDto.TotalSlots.Value * ratio);
                    updateBuilder = updateBuilder.Set(s => s.AvailableSlots, newAvailableSlots);
                }

                if (updateStationDto.PricePerHour.HasValue)
                    updateBuilder = updateBuilder.Set(s => s.PricePerHour, updateStationDto.PricePerHour.Value);

                if (updateStationDto.OperatingHours != null)
                {
                    updateBuilder = updateBuilder.Set(s => s.OperatingHours, new OperatingHours
                    {
                        OpenTime = updateStationDto.OperatingHours.OpenTime,
                        CloseTime = updateStationDto.OperatingHours.CloseTime,
                        OperatingDays = updateStationDto.OperatingHours.OperatingDays
                    });
                }

                if (updateStationDto.Amenities != null && updateStationDto.Amenities.Any())
                    updateBuilder = updateBuilder.Set(s => s.Amenities, updateStationDto.Amenities);

                var result = await _chargingStations.UpdateOneAsync(s => s.Id == id, updateBuilder);

                if (result.ModifiedCount > 0)
                {
                    var updatedStation = await _chargingStations.Find(s => s.Id == id).FirstOrDefaultAsync();
                    return new ApiResponseDTO<ChargingStation>
                    {
                        Success = true,
                        Message = "Charging station updated successfully",
                        Data = updatedStation
                    };
                }

                return new ApiResponseDTO<ChargingStation>
                {
                    Success = false,
                    Message = "Failed to update charging station"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<ChargingStation>
                {
                    Success = false,
                    Message = "An error occurred while updating the charging station"
                };
            }
        }

        /// <summary>
        /// Deletes a charging station from the system
        /// </summary>
        public async Task<ApiResponseDTO<bool>> DeleteChargingStationAsync(string id, string userId)
        {
            try
            {
                // Check if user has permission to delete the station
                //if (!await CanUserAccessStation(userId, id))
                //{
                //    return new ApiResponseDTO<bool>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to delete this charging station"
                //    };
                //}

                // Check if charging station has active bookings
                var activeBookings = await _bookings.Find(b =>
                    b.ChargingStationId == id &&
                    (b.Status == BookingStatus.Pending || b.Status == BookingStatus.Approved || b.Status == BookingStatus.InProgress)
                ).CountDocumentsAsync();

                if (activeBookings > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Cannot delete charging station with active bookings"
                    };
                }

                // Clear ChargingStationId from all assigned users before deleting
                var userCollection = _database.GetCollection<User>("Users");
                var updateUsers = Builders<User>.Update.Set(u => u.ChargingStationId, null);
                await userCollection.UpdateManyAsync(u => u.ChargingStationId == id, updateUsers);

                var result = await _chargingStations.DeleteOneAsync(s => s.Id == id);

                if (result.DeletedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "Charging station deleted successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Charging station not found"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while deleting the charging station"
                };
            }
        }

        /// <summary>
        /// Activates or deactivates a charging station
        /// </summary>
        public async Task<ApiResponseDTO<bool>> ActivateDeactivateChargingStationAsync(string id, bool isActive, string userId)
        {
            try
            {
                // Check if user has permission to update the station
                //if (!await CanUserAccessStation(userId, id))
                //{
                //    return new ApiResponseDTO<bool>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to update this charging station status"
                //    };
                //}

                if (!isActive)
                {
                    // Check if charging station has active bookings
                    var activeBookings = await _bookings.Find(b =>
                        b.ChargingStationId == id &&
                        (b.Status == BookingStatus.Pending || b.Status == BookingStatus.Approved || b.Status == BookingStatus.InProgress)
                    ).CountDocumentsAsync();

                    if (activeBookings > 0)
                    {
                        return new ApiResponseDTO<bool>
                        {
                            Success = false,
                            Message = "Cannot deactivate charging station with active bookings"
                        };
                    }
                }

                var update = Builders<ChargingStation>.Update
                    .Set(s => s.IsActive, isActive)
                    .Set(s => s.UpdatedAt, DateTime.UtcNow);

                var result = await _chargingStations.UpdateOneAsync(s => s.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = $"Charging station {(isActive ? "activated" : "deactivated")} successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Charging station not found"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while updating charging station status"
                };
            }
        }

        /// <summary>
        /// Gets nearby charging stations based on location and radius
        /// </summary>
        public async Task<ApiResponseDTO<List<ChargingStation>>> GetNearbyStationsAsync(NearbyStationsRequestDTO request)
        {
            try
            {
                var allStations = await _chargingStations.Find(s => s.IsActive).ToListAsync();
                var nearbyStations = new List<ChargingStation>();

                foreach (var station in allStations)
                {
                    var distance = CalculateDistance(request.Latitude, request.Longitude,
                                                   station.Location.Latitude, station.Location.Longitude);

                    if (distance <= request.RadiusKm)
                    {
                        nearbyStations.Add(station);
                    }
                }

                // Sort by distance (closest first)
                nearbyStations = nearbyStations.OrderBy(s =>
                    CalculateDistance(request.Latitude, request.Longitude,
                                    s.Location.Latitude, s.Location.Longitude)).ToList();

                return new ApiResponseDTO<List<ChargingStation>>
                {
                    Success = true,
                    Message = "Nearby stations retrieved successfully",
                    Data = nearbyStations
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<ChargingStation>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving nearby stations"
                };
            }
        }

        /// <summary>
        /// Updates slot availability for a charging station
        /// </summary>
        public async Task<ApiResponseDTO<bool>> UpdateSlotAvailabilityAsync(string stationId, int availableSlots, string userId)
        {
            try
            {
                // Check if user has permission to update the station
                //if (!await CanUserAccessStation(userId, stationId))
                //{
                //    return new ApiResponseDTO<bool>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to update this charging station's slots"
                //    };
                //}

                var station = await _chargingStations.Find(s => s.Id == stationId && s.IsActive).FirstOrDefaultAsync();
                if (station == null)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Charging station not found or has been deactivated"
                    };
                }

                if (availableSlots < 0 || availableSlots > station.TotalSlots)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Invalid slot count"
                    };
                }

                var update = Builders<ChargingStation>.Update
                    .Set(s => s.AvailableSlots, availableSlots)
                    .Set(s => s.UpdatedAt, DateTime.UtcNow);

                var result = await _chargingStations.UpdateOneAsync(s => s.Id == stationId, update);

                if (result.ModifiedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "Slot availability updated successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Failed to update slot availability"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while updating slot availability"
                };
            }
        }

        /// <summary>
        /// Assigns a station operator to a charging station
        /// </summary>
        public async Task<ApiResponseDTO<bool>> AssignStationOperatorAsync(string stationId, string operatorUserId, string backOfficeUserId)
        {
            try
            {
                // Check if BackOffice user owns this station
                //if (!await CanUserAccessStation(backOfficeUserId, stationId))
                //{
                //    return new ApiResponseDTO<bool>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to assign operators to this station"
                //    };
                //}

                var userCollection = _database.GetCollection<User>("Users");

                // Check if operator exists and is a StationOperator
                var operatorUser = await userCollection.Find(u => u.Id == operatorUserId && u.Role == UserRole.StationOperator).FirstOrDefaultAsync();
                if (operatorUser == null)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Station operator not found"
                    };
                }

                // Check if operator is already assigned to another station
                if (!string.IsNullOrEmpty(operatorUser.ChargingStationId))
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "This operator is already assigned to another charging station"
                    };
                }

                // Assign the operator to the station
                var update = Builders<User>.Update.Set(u => u.ChargingStationId, stationId);
                var result = await userCollection.UpdateOneAsync(u => u.Id == operatorUserId, update);

                if (result.ModifiedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "Station operator assigned successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Failed to assign station operator"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while assigning the station operator"
                };
            }
        }

        /// <summary>
        /// Revokes a station operator's assignment from a charging station
        /// </summary>
        public async Task<ApiResponseDTO<bool>> RevokeStationOperatorAsync(string stationId, string operatorUserId, string backOfficeUserId)
        {
            try
            {
                // Check if BackOffice user owns this station
                //if (!await CanUserAccessStation(backOfficeUserId, stationId))
                //{
                //    return new ApiResponseDTO<bool>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to revoke operators from this station"
                //    };
                //}

                var userCollection = _database.GetCollection<User>("Users");

                // Check if operator exists and is assigned to this station
                var operatorUser = await userCollection.Find(u =>
                    u.Id == operatorUserId &&
                    u.Role == UserRole.StationOperator &&
                    u.ChargingStationId == stationId
                ).FirstOrDefaultAsync();

                if (operatorUser == null)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Station operator not found or not assigned to this station"
                    };
                }

                // Remove the station assignment
                var update = Builders<User>.Update.Set(u => u.ChargingStationId, null);
                var result = await userCollection.UpdateOneAsync(u => u.Id == operatorUserId, update);

                if (result.ModifiedCount > 0)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "Station operator assignment revoked successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Failed to revoke station operator assignment"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while revoking the station operator assignment"
                };
            }
        }

        /// <summary>
        /// Gets all users assigned to a specific charging station
        /// </summary>
        public async Task<ApiResponseDTO<List<User>>> GetStationAssignedUsersAsync(string stationId, string backOfficeUserId)
        {
            try
            {
                //// Check if BackOffice user owns this station
                //if (!await CanUserAccessStation(backOfficeUserId, stationId))
                //{
                //    return new ApiResponseDTO<List<User>>
                //    {
                //        Success = false,
                //        Message = "You don't have permission to view users for this station"
                //    };
                //}

                var userCollection = _database.GetCollection<User>("Users");

                var assignedUsers = await userCollection.Find(u => u.ChargingStationId == stationId).ToListAsync();

                // Remove password hashes from response
                foreach (var user in assignedUsers)
                {
                    user.PasswordHash = string.Empty;
                }

                return new ApiResponseDTO<List<User>>
                {
                    Success = true,
                    Message = "Assigned users retrieved successfully",
                    Data = assignedUsers
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<User>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving assigned users"
                };
            }
        }

        /// <summary>
        /// Checks if a user has access to a specific charging station
        /// </summary>
        private async Task<bool> CanUserAccessStation(string userId, string stationId)
        {
            var userCollection = _database.GetCollection<User>("Users");
            var user = await userCollection.Find(u => u.Id == userId && u.ChargingStationId == stationId).FirstOrDefaultAsync();
            return user != null;
        }

        /// <summary>
        /// Calculates distance between two geographic points using Haversine formula
        /// </summary>
        private static double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371; // Earth's radius in kilometers

            var dLat = ToRadians(lat2 - lat1);
            var dLon = ToRadians(lon2 - lon1);

            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);

            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

            return R * c;
        }

        /// <summary>
        /// Converts degrees to radians
        /// </summary>
        private static double ToRadians(double degrees)
        {
            return degrees * (Math.PI / 180);
        }
    }
}
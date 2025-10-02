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

        /// <summary>
        /// Constructor to initialize charging station service with database collections
        /// </summary>
        public ChargingStationService(IMongoClient mongoClient)
        {
            var database = mongoClient.GetDatabase("EVChargingStationDB");
            _chargingStations = database.GetCollection<ChargingStation>("ChargingStations");
            _bookings = database.GetCollection<Booking>("Bookings");
        }

        /// <summary>
        /// Creates a new charging station
        /// </summary>
        public async Task<ApiResponseDTO<ChargingStation>> CreateChargingStationAsync(CreateChargingStationDTO createStationDto)
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
        public async Task<ApiResponseDTO<ChargingStation>> UpdateChargingStationAsync(string id, UpdateChargingStationDTO updateStationDto)
        {
            try
            {
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
        public async Task<ApiResponseDTO<bool>> DeleteChargingStationAsync(string id)
        {
            try
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
                        Message = "Cannot delete charging station with active bookings"
                    };
                }

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
        public async Task<ApiResponseDTO<bool>> ActivateDeactivateChargingStationAsync(string id, bool isActive)
        {
            try
            {
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
        public async Task<ApiResponseDTO<bool>> UpdateSlotAvailabilityAsync(string stationId, int availableSlots)
        {
            try
            {
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
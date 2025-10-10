// ========================================
// Services/BookingService.cs
// ========================================
/*
 * BookingService.cs
 * Booking service implementation
 * Date: September 2025
 * Description: Handles booking management and validation logic
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using MongoDB.Driver;
using Sprache;

namespace EVChargingStationAPI.Services
{
    public class BookingService : IBookingService
    {
        private readonly IMongoCollection<Booking> _bookings;
        private readonly IMongoCollection<ChargingStation> _chargingStations;
        private readonly IMongoCollection<EVOwner> _evOwners;
        private readonly IMongoCollection<User> _users;
        private readonly IQRService _qrService;

        /// <summary>
        /// Constructor to initialize booking service with database collections
        /// </summary>
        public BookingService(IMongoClient mongoClient, IQRService qrService)
        {
            var database = mongoClient.GetDatabase("EVChargingStationDB");
            _bookings = database.GetCollection<Booking>("Bookings");
            _chargingStations = database.GetCollection<ChargingStation>("ChargingStations");
            _evOwners = database.GetCollection<EVOwner>("EVOwners");
            _users = database.GetCollection<User>("Users");
            _qrService = qrService;
        }

        /// <summary>
        /// Creates a new booking reservation
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> CreateBookingAsync(string evOwnerNIC, CreateBookingDTO createBookingDto)
        {
            try
            {
                // Validate EV owner exists and is active
                var evOwner = await _evOwners.Find(e => e.NIC == evOwnerNIC && e.IsActive).FirstOrDefaultAsync();
                if (evOwner == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "EV Owner not found or inactive"
                    };
                }

                // Validate charging station exists and is active
                var chargingStation = await _chargingStations.Find(s => s.Id == createBookingDto.ChargingStationId && s.IsActive).FirstOrDefaultAsync();
                if (chargingStation == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Charging station not found or inactive"
                    };
                }

                // Validate reservation date/time is within 7 days from now
                var maxReservationDate = DateTime.Now.AddDays(7);
                if (createBookingDto.ReservationDateTime <= DateTime.Now ||
                    createBookingDto.ReservationDateTime > maxReservationDate)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Reservation date must be between now and 7 days from today"
                    };
                }

                // Check if charging station has available slots for the requested time
                var conflictingBookings = await _bookings.Find(b =>
                    b.ChargingStationId == createBookingDto.ChargingStationId &&
                    b.Status != BookingStatus.Cancelled &&
                    b.Status != BookingStatus.Completed &&
                    b.ReservationDateTime < createBookingDto.ReservationDateTime.AddMinutes(createBookingDto.DurationMinutes) &&
                    createBookingDto.ReservationDateTime < b.ReservationDateTime.AddMinutes(b.DurationMinutes)
                ).CountDocumentsAsync();

                if (conflictingBookings >= chargingStation.AvailableSlots)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "No available slots for the requested time"
                    };
                }

                // Calculate total amount
                var totalAmount = (decimal)(createBookingDto.DurationMinutes / 60.0) * chargingStation.PricePerHour;

                var booking = new Booking
                {
                    EVOwnerNIC = evOwnerNIC,
                    ChargingStationId = createBookingDto.ChargingStationId,
                    ReservationDateTime = createBookingDto.ReservationDateTime,
                    DurationMinutes = createBookingDto.DurationMinutes,
                    TotalAmount = totalAmount,
                    Notes = createBookingDto.Notes,
                    Status = BookingStatus.Pending
                };

                await _bookings.InsertOneAsync(booking);

                // Create response DTO
                var responseDto = new BookingResponseDTO
                {
                    Id = booking.Id,
                    EVOwnerNIC = booking.EVOwnerNIC,
                    ChargingStationId = chargingStation.Id,
                    ChargingStationName = chargingStation.Name,
                    ReservationDateTime = booking.ReservationDateTime,
                    DurationMinutes = booking.DurationMinutes,
                    Status = booking.Status,
                    TotalAmount = booking.TotalAmount,
                    QRCode = booking.QRCode,
                    CreatedAt = booking.CreatedAt
                };

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = true,
                    Message = "Booking created successfully",
                    Data = responseDto
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while creating the booking"
                };
            }
        }

        /// <summary>
        /// Retrieves all bookings from the system based on user role and assigned stations
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetAllBookingsAsync(string userId)
        {
            try
            {
                // Fetch user
                var user = await _users.Find(u => u.Id == userId).FirstOrDefaultAsync();
                if (user == null)
                {
                    return new ApiResponseDTO<List<BookingResponseDTO>>
                    {
                        Success = false,
                        Message = "User not found"
                    };
                }

                List<Booking> bookings;

                // BackOffice: get all bookings
                if (user.Role == UserRole.BackOffice)
                {
                    bookings = await _bookings.Find(_ => true).ToListAsync();
                }
                else
                {
                    // Station Operator: must have assigned stations
                    if (user.ChargingStationIds == null || !user.ChargingStationIds.Any())
                    {
                        return new ApiResponseDTO<List<BookingResponseDTO>>
                        {
                            Success = false,
                            Message = "Station Operator is not assigned to any charging stations"
                        };
                    }

                    // Get bookings only for assigned station IDs
                    bookings = await _bookings
                        .Find(b => user.ChargingStationIds.Contains(b.ChargingStationId))
                        .ToListAsync();
                }

                // Build booking responses
                var bookingResponses = new List<BookingResponseDTO>();
                foreach (var booking in bookings)
                {
                    var chargingStation = await _chargingStations
                        .Find(s => s.Id == booking.ChargingStationId)
                        .FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "Bookings retrieved successfully",
                    Data = bookingResponses
                };
            }
            catch (Exception ex)
            {
                // Optional: log ex for debugging
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving bookings"
                };
            }
        }

        /// <summary>
        /// Retrieves all bookings from the system
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetAllBookingsAsync2()
        {
            try
            {

                var bookings = await _bookings.Find(_ => true).ToListAsync();
                var bookingResponses = new List<BookingResponseDTO>();

                foreach (var booking in bookings)
                {
                    var chargingStation = await _chargingStations
                                    .Find(s => s.Id == booking.ChargingStationId)
                                    .FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "Bookings retrieved successfully",
                    Data = bookingResponses
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving bookings"
                };
            }
        }

        /// <summary>
        /// Retrieves bookings for a specific station operator
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetBookingsByStationOperatorAsync(string operatorId)
        {
            try
            {
                var user = await _users.Find(u => u.Id == operatorId).FirstOrDefaultAsync();
                if (user == null)
                {
                    return new ApiResponseDTO<List<BookingResponseDTO>>
                    {
                        Success = false,
                        Message = "Operator not found"
                    };
                }

                if (user.ChargingStationIds == null || !user.ChargingStationIds.Any())
                {
                    return new ApiResponseDTO<List<BookingResponseDTO>>
                    {
                        Success = true,
                        Message = "Not assigned to any charging stations",
                        Data = new List<BookingResponseDTO>()
                    };
                }

                var bookings = await _bookings
                    .Find(b => user.ChargingStationIds.Contains(b.ChargingStationId))
                    .ToListAsync();

                var bookingResponses = new List<BookingResponseDTO>();
                foreach (var booking in bookings)
                {
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "Bookings retrieved successfully",
                    Data = bookingResponses
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving bookings"
                };
            }
        }

        /// <summary>
        /// Retrieves bookings for a specific EV owner
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetBookingsByEVOwnerAsync(string evOwnerNIC)
        {
            try
            {
                var bookings = await _bookings.Find(b => b.EVOwnerNIC == evOwnerNIC).ToListAsync();
                var bookingResponses = new List<BookingResponseDTO>();

                foreach (var booking in bookings)
                {
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "EV Owner bookings retrieved successfully",
                    Data = bookingResponses.OrderByDescending(b => b.CreatedAt).ToList()
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving EV owner bookings"
                };
            }
        }

        /// <summary>
        /// Retrieves a booking by its ID
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> GetBookingByIdAsync(string id)
        {
            try
            {
                var booking = await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Booking not found"
                    };
                }

                var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                var responseDto = new BookingResponseDTO
                {
                    Id = booking.Id,
                    EVOwnerNIC = booking.EVOwnerNIC,
                    ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                    ReservationDateTime = booking.ReservationDateTime,
                    DurationMinutes = booking.DurationMinutes,
                    Status = booking.Status,
                    TotalAmount = booking.TotalAmount,
                    QRCode = booking.QRCode,
                    CreatedAt = booking.CreatedAt
                };

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = true,
                    Message = "Booking retrieved successfully",
                    Data = responseDto
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while retrieving the booking"
                };
            }
        }

        /// <summary>
        /// Updates an existing booking (at least 12 hours before reservation)
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> UpdateBookingAsync(string id, string evOwnerNIC, UpdateBookingDTO updateBookingDto)
        {
            try
            {
                var booking = await _bookings.Find(b => b.Id == id && b.EVOwnerNIC == evOwnerNIC).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Booking not found or you don't have permission to modify it"
                    };
                }

                // Check if booking can be modified (at least 12 hours before reservation and not completed/cancelled)
                var minimumUpdateTime = booking.ReservationDateTime.AddHours(-12);
                if (DateTime.UtcNow >= minimumUpdateTime)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Booking can only be modified at least 12 hours before the reservation time"
                    };
                }

                if (booking.Status == BookingStatus.Completed || booking.Status == BookingStatus.Cancelled)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Cannot modify completed or cancelled bookings"
                    };
                }

                var updateBuilder = Builders<Booking>.Update.Set(b => b.UpdatedAt, DateTime.UtcNow);

                // Update reservation date/time if provided
                if (updateBookingDto.ReservationDateTime.HasValue)
                {
                    var maxReservationDate = DateTime.Now.AddDays(7);
                    if (updateBookingDto.ReservationDateTime.Value <= DateTime.Now ||
                        updateBookingDto.ReservationDateTime.Value > maxReservationDate)
                    {
                        return new ApiResponseDTO<BookingResponseDTO>
                        {
                            Success = false,
                            Message = "Reservation date must be between now and 7 days from today"
                        };
                    }
                    updateBuilder = updateBuilder.Set(b => b.ReservationDateTime, updateBookingDto.ReservationDateTime.Value);
                }

                // Update duration if provided
                if (updateBookingDto.DurationMinutes.HasValue)
                {
                    updateBuilder = updateBuilder.Set(b => b.DurationMinutes, updateBookingDto.DurationMinutes.Value);

                    // Recalculate total amount
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();
                    if (chargingStation != null)
                    {
                        var newTotalAmount = (decimal)(updateBookingDto.DurationMinutes.Value / 60.0) * chargingStation.PricePerHour;
                        updateBuilder = updateBuilder.Set(b => b.TotalAmount, newTotalAmount);
                    }
                }

                // Update notes if provided
                if (!string.IsNullOrEmpty(updateBookingDto.Notes))
                    updateBuilder = updateBuilder.Set(b => b.Notes, updateBookingDto.Notes);

                var result = await _bookings.UpdateOneAsync(b => b.Id == id, updateBuilder);

                if (result.ModifiedCount > 0)
                {
                    var updatedBooking = await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
                    var chargingStation = await _chargingStations.Find(s => s.Id == updatedBooking.ChargingStationId).FirstOrDefaultAsync();

                    var responseDto = new BookingResponseDTO
                    {
                        Id = updatedBooking.Id,
                        EVOwnerNIC = updatedBooking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = updatedBooking.ReservationDateTime,
                        DurationMinutes = updatedBooking.DurationMinutes,
                        Status = updatedBooking.Status,
                        TotalAmount = updatedBooking.TotalAmount,
                        QRCode = updatedBooking.QRCode,
                        CreatedAt = updatedBooking.CreatedAt
                    };

                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = true,
                        Message = "Booking updated successfully",
                        Data = responseDto
                    };
                }

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "Failed to update booking"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while approving the booking"
                };
            }
        }

        /// <summary>
        /// Cancels a booking (at least 12 hours before reservation)
        /// </summary>
        public async Task<ApiResponseDTO<bool>> CancelBookingAsync(string id, string evOwnerNIC)
        {
            try
            {
                var booking = await _bookings.Find(b => b.Id == id && b.EVOwnerNIC == evOwnerNIC).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Booking not found or you don't have permission to cancel it"
                    };
                }

                // Check if booking can be cancelled (at least 12 hours before reservation and not already completed/cancelled)
                var minimumCancelTime = booking.ReservationDateTime.AddHours(-12);
                if (DateTime.UtcNow >= minimumCancelTime)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Booking can only be cancelled at least 12 hours before the reservation time"
                    };
                }

                if (booking.Status == BookingStatus.Completed || booking.Status == BookingStatus.Cancelled)
                {
                    return new ApiResponseDTO<bool>
                    {
                        Success = false,
                        Message = "Booking is already completed or cancelled"
                    };
                }

                // Store the original status BEFORE updating
                var originalStatus = booking.Status;

                var update = Builders<Booking>.Update
                    .Set(b => b.Status, BookingStatus.Cancelled)
                    .Set(b => b.UpdatedAt, DateTime.UtcNow);

                var result = await _bookings.UpdateOneAsync(b => b.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    // If booking was approved, return the slot
                    if (originalStatus == BookingStatus.Approved || originalStatus == BookingStatus.InProgress)
                    {
                        var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();
                        if (chargingStation != null && chargingStation.AvailableSlots < chargingStation.TotalSlots)
                        {
                            var slotUpdate = Builders<ChargingStation>.Update
                                .Set(s => s.AvailableSlots, chargingStation.AvailableSlots + 1)
                                .Set(s => s.UpdatedAt, DateTime.UtcNow);
                            await _chargingStations.UpdateOneAsync(s => s.Id == booking.ChargingStationId, slotUpdate);
                        }
                    }

                    return new ApiResponseDTO<bool>
                    {
                        Success = true,
                        Message = "Booking cancelled successfully",
                        Data = true
                    };
                }

                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "Failed to cancel booking"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<bool>
                {
                    Success = false,
                    Message = "An error occurred while cancelling the booking"
                };
            }
        }

        /// <summary>
        /// Approves a pending booking and generates QR code
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> ApproveBookingAsync(string id, string userId)
        {
            try
            {
                // Check authorization
                if (!await CanUserAccessBooking(userId, id))
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "You don't have permission to approve this booking"
                    };
                }

                var booking = await _bookings.Find(b => b.Id == id && b.Status == BookingStatus.Pending).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Pending booking not found"
                    };
                }

                // Generate QR code for the booking
                var qrCode = _qrService.GenerateQRCode(booking);

                var update = Builders<Booking>.Update
                    .Set(b => b.Status, BookingStatus.Approved)
                    .Set(b => b.QRCode, qrCode)
                    .Set(b => b.UpdatedAt, DateTime.UtcNow);

                var result = await _bookings.UpdateOneAsync(b => b.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    // Reduce available slots by 1
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();
                    if (chargingStation != null && chargingStation.AvailableSlots > 0)
                    {
                        var slotUpdate = Builders<ChargingStation>.Update
                            .Set(s => s.AvailableSlots, chargingStation.AvailableSlots - 1)
                            .Set(s => s.UpdatedAt, DateTime.UtcNow);
                        await _chargingStations.UpdateOneAsync(s => s.Id == booking.ChargingStationId, slotUpdate);
                    }

                    var updatedBooking = await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
                    var chargingStationForResponse = await _chargingStations.Find(s => s.Id == updatedBooking.ChargingStationId).FirstOrDefaultAsync();

                    var responseDto = new BookingResponseDTO
                    {
                        Id = updatedBooking.Id,
                        EVOwnerNIC = updatedBooking.EVOwnerNIC,
                        ChargingStationName = chargingStationForResponse?.Name ?? "Unknown Station",
                        ReservationDateTime = updatedBooking.ReservationDateTime,
                        DurationMinutes = updatedBooking.DurationMinutes,
                        Status = updatedBooking.Status,
                        TotalAmount = updatedBooking.TotalAmount,
                        QRCode = updatedBooking.QRCode,
                        CreatedAt = updatedBooking.CreatedAt
                    };

                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = true,
                        Message = "Booking approved successfully",
                        Data = responseDto
                    };
                }

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "Failed to approve booking"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while approving the booking"
                };
            }
        }

        /// <summary>
        /// Starts a booking session
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> StartBookingAsync(string id, string userId)
        {
            try
            {
                // Check authorization
                if (!await CanUserAccessBooking(userId, id))
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "You don't have permission to start this booking"
                    };
                }

                var booking = await _bookings.Find(b => b.Id == id && b.Status == BookingStatus.Approved).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "Approved booking not found"
                    };
                }

                var update = Builders<Booking>.Update
                    .Set(b => b.Status, BookingStatus.InProgress)
                    .Set(b => b.ActualStartTime, DateTime.UtcNow)
                    .Set(b => b.UpdatedAt, DateTime.UtcNow);

                var result = await _bookings.UpdateOneAsync(b => b.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    var updatedBooking = await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
                    var chargingStation = await _chargingStations.Find(s => s.Id == updatedBooking.ChargingStationId).FirstOrDefaultAsync();

                    var responseDto = new BookingResponseDTO
                    {
                        Id = updatedBooking.Id,
                        EVOwnerNIC = updatedBooking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = updatedBooking.ReservationDateTime,
                        DurationMinutes = updatedBooking.DurationMinutes,
                        Status = updatedBooking.Status,
                        TotalAmount = updatedBooking.TotalAmount,
                        QRCode = updatedBooking.QRCode,
                        CreatedAt = updatedBooking.CreatedAt
                    };

                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = true,
                        Message = "Booking started successfully",
                        Data = responseDto
                    };
                }

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "Failed to start booking"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while starting the booking"
                };
            }
        }

        /// <summary>
        /// Completes a booking session
        /// </summary>
        public async Task<ApiResponseDTO<BookingResponseDTO>> CompleteBookingAsync(string id, string userId)
        {
            try
            {
                // Check authorization
                if (!await CanUserAccessBooking(userId, id))
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "You don't have permission to complete this booking"
                    };
                }

                var booking = await _bookings.Find(b => b.Id == id && b.Status == BookingStatus.InProgress).FirstOrDefaultAsync();

                if (booking == null)
                {
                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = false,
                        Message = "In-progress booking not found"
                    };
                }

                var update = Builders<Booking>.Update
                    .Set(b => b.Status, BookingStatus.Completed)
                    .Set(b => b.ActualEndTime, DateTime.UtcNow)
                    .Set(b => b.UpdatedAt, DateTime.UtcNow);

                var result = await _bookings.UpdateOneAsync(b => b.Id == id, update);

                if (result.ModifiedCount > 0)
                {
                    // Increase available slots by 1
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();
                    if (chargingStation != null && chargingStation.AvailableSlots < chargingStation.TotalSlots)
                    {
                        var slotUpdate = Builders<ChargingStation>.Update
                            .Set(s => s.AvailableSlots, chargingStation.AvailableSlots + 1)
                            .Set(s => s.UpdatedAt, DateTime.UtcNow);
                        await _chargingStations.UpdateOneAsync(s => s.Id == booking.ChargingStationId, slotUpdate);
                    }

                    var updatedBooking = await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
                    var chargingStationForResponse = await _chargingStations.Find(s => s.Id == updatedBooking.ChargingStationId).FirstOrDefaultAsync();

                    var responseDto = new BookingResponseDTO
                    {
                        Id = updatedBooking.Id,
                        EVOwnerNIC = updatedBooking.EVOwnerNIC,
                        ChargingStationName = chargingStationForResponse?.Name ?? "Unknown Station",
                        ReservationDateTime = updatedBooking.ReservationDateTime,
                        DurationMinutes = updatedBooking.DurationMinutes,
                        Status = updatedBooking.Status,
                        TotalAmount = updatedBooking.TotalAmount,
                        QRCode = updatedBooking.QRCode,
                        CreatedAt = updatedBooking.CreatedAt
                    };

                    return new ApiResponseDTO<BookingResponseDTO>
                    {
                        Success = true,
                        Message = "Booking completed successfully",
                        Data = responseDto
                    };
                }

                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "Failed to complete booking"
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<BookingResponseDTO>
                {
                    Success = false,
                    Message = "An error occurred while completing the booking"
                };
            }
        }

        /// <summary>
        /// Gets upcoming bookings for an EV owner
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetUpcomingBookingsAsync(string evOwnerNIC)
        {
            try
            {
                var upcomingBookings = await _bookings.Find(b =>
                    b.EVOwnerNIC == evOwnerNIC &&
                    (b.Status == BookingStatus.Approved || b.Status == BookingStatus.Pending) &&
                    b.ReservationDateTime > DateTime.UtcNow
                ).ToListAsync();

                var bookingResponses = new List<BookingResponseDTO>();

                foreach (var booking in upcomingBookings)
                {
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "Upcoming bookings retrieved successfully",
                    Data = bookingResponses.OrderBy(b => b.ReservationDateTime).ToList()
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving upcoming bookings"
                };
            }
        }

        /// <summary>
        /// Gets booking history for an EV owner
        /// </summary>
        public async Task<ApiResponseDTO<List<BookingResponseDTO>>> GetBookingHistoryAsync(string evOwnerNIC)
        {
            try
            {
                var historicalBookings = await _bookings.Find(b =>
                    b.EVOwnerNIC == evOwnerNIC &&
                    (b.Status == BookingStatus.Completed || b.Status == BookingStatus.Cancelled)
                ).ToListAsync();

                var bookingResponses = new List<BookingResponseDTO>();

                foreach (var booking in historicalBookings)
                {
                    var chargingStation = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                    bookingResponses.Add(new BookingResponseDTO
                    {
                        Id = booking.Id,
                        EVOwnerNIC = booking.EVOwnerNIC,
                        ChargingStationName = chargingStation?.Name ?? "Unknown Station",
                        ReservationDateTime = booking.ReservationDateTime,
                        DurationMinutes = booking.DurationMinutes,
                        Status = booking.Status,
                        TotalAmount = booking.TotalAmount,
                        QRCode = booking.QRCode,
                        CreatedAt = booking.CreatedAt
                    });
                }

                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = true,
                    Message = "Booking history retrieved successfully",
                    Data = bookingResponses.OrderByDescending(b => b.ReservationDateTime).ToList()
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDTO<List<BookingResponseDTO>>
                {
                    Success = false,
                    Message = "An error occurred while retrieving booking history"
                };
            }
        }

        /// <summary>
                /// Gets enhanced session history with EV owner details for station operators
                /// </summary>
                public async Task<ApiResponseDTO<List<SessionHistoryDTO>>> GetSessionHistoryAsync()
                {
                    try
                    {
                        // Get all bookings with completed or in-progress status
                        var bookings = await _bookings.Find(b =>
                            b.Status == BookingStatus.Completed ||
                            b.Status == BookingStatus.InProgress ||
                            b.Status == BookingStatus.Cancelled ||
                            b.Status == BookingStatus.NoShow)
                            .ToListAsync();

                        var sessionHistoryList = new List<SessionHistoryDTO>();

                        foreach (var booking in bookings)
                        {
                            // Get EV Owner details
                            var evOwner = await _evOwners.Find(e => e.NIC == booking.EVOwnerNIC).FirstOrDefaultAsync();

                            // Get Charging Station details
                            var station = await _chargingStations.Find(s => s.Id == booking.ChargingStationId).FirstOrDefaultAsync();

                            // Calculate energy delivered (mock calculation based on duration and status)
                            double? energyDelivered = null;
                            if (booking.Status == BookingStatus.Completed && booking.ActualStartTime.HasValue && booking.ActualEndTime.HasValue)
                            {
                                var actualDuration = (booking.ActualEndTime.Value - booking.ActualStartTime.Value).TotalMinutes;
                                energyDelivered = Math.Round(actualDuration / 60.0 * 7.5, 2); // 7.5 kWh per hour
                            }

                            var sessionHistory = new SessionHistoryDTO
                            {
                                BookingId = booking.Id,
                                EVOwnerName = evOwner != null ? $"{evOwner.FirstName} {evOwner.LastName}".Trim() : "Unknown Customer",
                                EVOwnerNIC = booking.EVOwnerNIC,
                                EVOwnerPhone = evOwner?.PhoneNumber ?? "N/A",
                                ChargingStationId = booking.ChargingStationId,
                                ChargingStationName = station?.Name ?? $"Station {booking.ChargingStationId}",
                                ReservationDateTime = booking.ReservationDateTime,
                                DurationMinutes = booking.DurationMinutes,
                                Status = booking.Status,
                                StatusDisplayName = GetStatusDisplayName(booking.Status),
                                TotalAmount = booking.TotalAmount,
                                ActualStartTime = booking.ActualStartTime,
                                ActualEndTime = booking.ActualEndTime,
                                EnergyDelivered = energyDelivered,
                                Notes = booking.Notes,
                                CreatedAt = booking.CreatedAt,
                                CustomerVehicles = evOwner?.VehicleDetails?.Select(v => new VehicleDetailDTO
                                {
                                    Make = v.Make,
                                    Model = v.Model,
                                    LicensePlate = v.LicensePlate,
                                    Year = v.Year
                                }).ToList() ?? new List<VehicleDetailDTO>()
                            };

                            sessionHistoryList.Add(sessionHistory);
                        }

                        return new ApiResponseDTO<List<SessionHistoryDTO>>
                        {
                            Success = true,
                            Message = "Session history retrieved successfully",
                            Data = sessionHistoryList.OrderByDescending(s => s.CreatedAt).ToList()
                        };
                    }
                    catch (Exception ex)
                    {
                        return new ApiResponseDTO<List<SessionHistoryDTO>>
                        {
                            Success = false,
                            Message = "An error occurred while retrieving session history"
                        };
                    }
                }



        /// <summary>
        /// Checks if a user can access a specific booking based on their associated charging stations
        /// </summary>
        private async Task<bool> CanUserAccessBooking(string userId, string bookingId)
        {
            var booking = await _bookings.Find(b => b.Id == bookingId).FirstOrDefaultAsync();
            if (booking == null) return false;

            var user = await _users.Find(u => u.Id == userId).FirstOrDefaultAsync();
            if (user == null) return false;

            // BackOffice users can access any booking
            if (user.Role == UserRole.BackOffice)
                return true;

            // Station Operators must match one of their assigned stations
            return user.ChargingStationIds != null && user.ChargingStationIds.Contains(booking.ChargingStationId);
        }

        /// <summary>
                /// Gets user-friendly status display name
                /// </summary>
                private string GetStatusDisplayName(BookingStatus status)
                {
                    return status switch
                    {
                        BookingStatus.Pending => "Pending Approval",
                        BookingStatus.Approved => "Approved",
                        BookingStatus.InProgress => "In Progress",
                        BookingStatus.Completed => "Completed",
                        BookingStatus.Cancelled => "Cancelled",
                        BookingStatus.NoShow => "No Show",
                        _ => "Unknown"
                    };
                }

    }
}
// ========================================
// Controllers/BookingsController.cs
// ========================================
/*
 * BookingsController.cs
 * Booking management controller
 * Date: September 2025
 * Description: Handles booking operations for EV owners and station operators
 */

using EVChargingStationAPI.Models.DTOs;
using EVChargingStationAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace EVChargingStationAPI.Controllers
{
    [ApiController]
    [Route("api/bookings")]
    [Authorize]
    public class BookingsController : ControllerBase
    {
        private readonly IBookingService _bookingService;
        private readonly IQRService _qrService;

        /// <summary>
        /// Constructor to initialize bookings controller
        /// </summary>
        public BookingsController(IBookingService bookingService, IQRService qrService)
        {
            _bookingService = bookingService;
            _qrService = qrService;
        }

        /// <summary>
        /// Creates a new booking reservation (EV Owners only)
        /// </summary>
        [HttpPost]
        [Authorize(Roles = "EVOwner")]
        public async Task<IActionResult> CreateBooking([FromBody] CreateBookingDTO createBookingDto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                // Get EV Owner NIC from token (you'll need to modify this based on your JWT setup)
                var evOwnerNIC = User.FindFirst("NIC")?.Value; // Assuming NIC is stored in token
                if (string.IsNullOrEmpty(evOwnerNIC))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "EV Owner NIC not found in token"
                    });
                }

                var result = await _bookingService.CreateBookingAsync(evOwnerNIC, createBookingDto);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Gets all bookings (BackOffice and Station Operators)
        /// </summary>
        //[HttpGet]
        //[Authorize(Roles = "BackOffice,StationOperator")]
        //public async Task<IActionResult> GetAllBookings()
        //{
        //    try
        //    {
        //        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        //        if (string.IsNullOrEmpty(userId))
        //        {
        //            return BadRequest(new ApiResponseDTO<object>
        //            {
        //                Success = false,
        //                Message = "User ID not found in token"
        //            });
        //        }

        //        var result = await _bookingService.GetAllBookingsAsync(userId);
        //        Console.WriteLine("empty naha" + result);
        //        return Ok(result);
        //    }
        //    catch (Exception ex)
        //    {
        //        return StatusCode(500, new ApiResponseDTO<object>
        //        {
        //            Success = false,
        //            Message = "An internal error occurred"
        //        });
        //    }
        //}



        /// <summary>
        /// Gets all bookings (BackOffice and Station Operators)
        /// </summary>
        [HttpGet]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> GetAllBookings2()
        {
            try
            {
                var result = await _bookingService.GetAllBookingsAsync2();
                Console.WriteLine("empty naha" + result);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }
        /// <summary>
        /// Gets bookings for a specific EV owner
        /// </summary>
        [HttpGet("evowner/{nic}")]
        [Authorize]
        public async Task<IActionResult> GetBookingsByEVOwner(string nic)
        {
            try
            {
                // Check if user is accessing their own bookings or is BackOffice/Station Operator
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                var userRole = User.FindFirst(ClaimTypes.Role)?.Value;

                if (currentUserNIC != nic && userRole != "BackOffice" && userRole != "StationOperator")
                {
                    return Forbid();
                }

                var result = await _bookingService.GetBookingsByEVOwnerAsync(nic);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Gets a booking by ID
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> GetBookingById(string id)
        {
            try
            {
                var result = await _bookingService.GetBookingByIdAsync(id);

                if (result.Success)
                {
                    return Ok(result);
                }

                return NotFound(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Updates a booking (EV Owners only, for their own bookings)
        /// </summary>
        [HttpPut("{id}")]
        [Authorize(Roles = "EVOwner")]
        public async Task<IActionResult> UpdateBooking(string id, [FromBody] UpdateBookingDTO updateBookingDto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                var evOwnerNIC = User.FindFirst("NIC")?.Value;
                if (string.IsNullOrEmpty(evOwnerNIC))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "EV Owner NIC not found in token"
                    });
                }

                var result = await _bookingService.UpdateBookingAsync(id, evOwnerNIC, updateBookingDto);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Cancels a booking (EV Owners only, for their own bookings)
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize(Roles = "EVOwner")]
        public async Task<IActionResult> CancelBooking(string id)
        {
            try
            {
                var evOwnerNIC = User.FindFirst("NIC")?.Value;
                if (string.IsNullOrEmpty(evOwnerNIC))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "EV Owner NIC not found in token"
                    });
                }

                var result = await _bookingService.CancelBookingAsync(id, evOwnerNIC);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Approves a pending booking (BackOffice and Station Operators)
        /// </summary>
        [HttpPatch("{id}/approve")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> ApproveBooking(string id)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "User ID not found in token"
                    });
                }

                var result = await _bookingService.ApproveBookingAsync(id, userId);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Starts a booking session (Station Operators)
        /// </summary>
        [HttpPatch("{id}/start")]
        [Authorize(Roles = "StationOperator")]
        public async Task<IActionResult> StartBooking(string id)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "User ID not found in token"
                    });
                }

                var result = await _bookingService.StartBookingAsync(id, userId);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Completes a booking session (Station Operators)
        /// </summary>
        [HttpPatch("{id}/complete")]
        [Authorize(Roles = "StationOperator")]
        public async Task<IActionResult> CompleteBooking(string id)
        {
            try
            {
                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "User ID not found in token"
                    });
                }

                var result = await _bookingService.CompleteBookingAsync(id, userId);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Gets upcoming bookings for an EV owner
        /// </summary>
        [HttpGet("evowner/{nic}/upcoming")]
        [Authorize(Roles = "EVOwner")]
        public async Task<IActionResult> GetUpcomingBookings(string nic)
        {
            try
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (currentUserNIC != nic)
                {
                    return Forbid();
                }

                var result = await _bookingService.GetUpcomingBookingsAsync(nic);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Gets booking history for an EV owner
        /// </summary>
        [HttpGet("evowner/{nic}/history")]
        [Authorize(Roles = "EVOwner")]
        public async Task<IActionResult> GetBookingHistory(string nic)
        {
            try
            {
                var currentUserNIC = User.FindFirst("NIC")?.Value;
                if (currentUserNIC != nic)
                {
                    return Forbid();
                }

                var result = await _bookingService.GetBookingHistoryAsync(nic);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Verifies QR code for booking (Station Operators)
        /// </summary>
        [HttpPost("verify-qr")]
        [Authorize(Roles = "StationOperator")]
        public async Task<IActionResult> VerifyQRCode([FromBody] QRVerificationDTO qrVerificationDto)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "Invalid request data",
                        Errors = ModelState.Values.SelectMany(v => v.Errors.Select(e => e.ErrorMessage)).ToList()
                    });
                }

                var result = await _qrService.VerifyQRCodeAsync(qrVerificationDto.QRCode);

                if (result.Success)
                {
                    return Ok(result);
                }

                return BadRequest(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }

        /// <summary>
        /// Gets enhanced session history for station operators with EV owner details
        /// </summary>
        [HttpGet("session-history")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> GetSessionHistory()
        {
            try
            {
                var result = await _bookingService.GetSessionHistoryAsync();
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ApiResponseDTO<object>
                {
                    Success = false,
                    Message = "An internal error occurred"
                });
            }
        }
    }
}
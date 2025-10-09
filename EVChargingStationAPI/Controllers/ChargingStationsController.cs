// ========================================
// Controllers/ChargingStationsController.cs
// ========================================
/*
 * ChargingStationsController.cs
 * Charging station management controller
 * Date: September 2025
 * Description: Handles charging station management and location-based operations
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;
using EVChargingStationAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace EVChargingStationAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ChargingStationsController : ControllerBase
    {
        private readonly IChargingStationService _chargingStationService;

        /// <summary>
        /// Constructor to initialize charging stations controller
        /// </summary>
        public ChargingStationsController(IChargingStationService chargingStationService)
        {
            _chargingStationService = chargingStationService;
        }

        /// <summary>
        /// Creates a new charging station (BackOffice only)
        /// </summary>
        [HttpPost]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> CreateChargingStation([FromBody] CreateChargingStationDTO createStationDto)
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

                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "User ID not found in token"
                    });
                }

                var result = await _chargingStationService.CreateChargingStationAsync(createStationDto, userId);

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
        /// Gets all charging stations
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetAllChargingStations()
        {
            try
            {
                var result = await _chargingStationService.GetAllChargingStationsAsync();
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
        /// Gets a charging station by ID
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> GetChargingStationById(string id)
        {
            try
            {
                var result = await _chargingStationService.GetChargingStationByIdAsync(id);

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
        /// Updates a charging station (BackOffice and Station Operators)
        /// </summary>
        [HttpPut("{id}")]
        [Authorize(Roles = "BackOffice,StationOperator")]
        public async Task<IActionResult> UpdateChargingStation(string id, [FromBody] UpdateChargingStationDTO updateStationDto)
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

                var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrEmpty(userId))
                {
                    return BadRequest(new ApiResponseDTO<object>
                    {
                        Success = false,
                        Message = "User ID not found in token"
                    });
                }

                var result = await _chargingStationService.UpdateChargingStationAsync(id, updateStationDto, userId);

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
        /// Gets nearby charging stations based on location
        /// </summary>
        [HttpPost("nearby")]
        public async Task<IActionResult> GetNearbyStations([FromBody] NearbyStationsRequestDTO request)
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

                var result = await _chargingStationService.GetNearbyStationsAsync(request);
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
        /// Updates slot availability for a charging station (Station Operators)
        /// </summary>
        [HttpPatch("{id}/slots")]
        [Authorize(Roles = "StationOperator,BackOffice")]
        public async Task<IActionResult> UpdateSlotAvailability(string id, [FromQuery] int availableSlots)
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

                var result = await _chargingStationService.UpdateSlotAvailabilityAsync(id, availableSlots, userId);

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
        /// Activates or deactivates a charging station (BackOffice only)
        /// </summary>
        [HttpPatch("{id}/status")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> UpdateStationStatus(string id, [FromQuery] bool isActive)
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

                var result = await _chargingStationService.ActivateDeactivateChargingStationAsync(id, isActive, userId);

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
        /// Deletes a charging station (BackOffice only)
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> DeleteChargingStation(string id)
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

                var result = await _chargingStationService.DeleteChargingStationAsync(id, userId);

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
        /// Assigns a station operator to a charging station (BackOffice only)
        /// </summary>
        [HttpPost("{stationId}/assign-operator")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> AssignStationOperator(string stationId, [FromQuery] string operatorUserId)
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

                var result = await _chargingStationService.AssignStationOperatorAsync(stationId, operatorUserId, userId);

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
        /// Revokes a station operator's assignment from a charging station (BackOffice only)
        /// </summary>
        [HttpPost("{stationId}/revoke-operator")]
        [Authorize(Roles = "BackOffice")]
        public async Task<IActionResult> RevokeStationOperator(string stationId, [FromQuery] string operatorUserId)
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

                var result = await _chargingStationService.RevokeStationOperatorAsync(stationId, operatorUserId, userId);

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
        /// Gets all users assigned to a charging station (BackOffice only)
        /// </summary>
        [HttpGet("{stationId}/assigned-users")]
        [Authorize(Roles = "StationOperator,BackOffice")]
        public async Task<IActionResult> GetStationAssignedUsers(string stationId)
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

                var result = await _chargingStationService.GetStationAssignedUsersAsync(stationId, userId);

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
    }
}
// ========================================
// Services/Interfaces/IChargingStationService.cs
// ========================================
/*
 * IChargingStationService.cs
 * Charging Station service interface
 * Date: September 2025
 * Description: Defines charging station management service contract
 */

using EVChargingStationAPI.Models;
using EVChargingStationAPI.Models.DTOs;

namespace EVChargingStationAPI.Services
{
    public interface IChargingStationService
    {
        Task<ApiResponseDTO<ChargingStation>> CreateChargingStationAsync(CreateChargingStationDTO createStationDto, string backOfficeUserId);
        Task<ApiResponseDTO<List<ChargingStation>>> GetAllChargingStationsAsync();
        Task<ApiResponseDTO<ChargingStation>> GetChargingStationByIdAsync(string id);
        Task<ApiResponseDTO<ChargingStation>> UpdateChargingStationAsync(string id, UpdateChargingStationDTO updateStationDto, string userId);
        Task<ApiResponseDTO<bool>> DeleteChargingStationAsync(string id, string userId);
        Task<ApiResponseDTO<bool>> ActivateDeactivateChargingStationAsync(string id, bool isActive, string userId);
        Task<ApiResponseDTO<List<ChargingStation>>> GetNearbyStationsAsync(NearbyStationsRequestDTO request);
        Task<ApiResponseDTO<bool>> UpdateSlotAvailabilityAsync(string stationId, int availableSlots, string userId);
        Task<ApiResponseDTO<bool>> AssignStationOperatorAsync(string stationId, string operatorUserId, string backOfficeUserId);
    }
}
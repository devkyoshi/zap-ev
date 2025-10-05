package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.api.services.ChargingStationApiService;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service class for handling charging station operations
 */
public class ChargingStationService {
    private static final String TAG = "ChargingStationService";
    
    private final Context context;
    private final ChargingStationApiService stationApiService;
    private final PreferenceManager preferenceManager;

    // Constructor
    public ChargingStationService(Context context) {
        this.context = context.getApplicationContext();
        this.stationApiService = NetworkClient.getInstance(context).createService(ChargingStationApiService.class);
        this.preferenceManager = new PreferenceManager(context);
    }

    // Interface for callbacks
    public interface ChargingStationsCallback {
        void onSuccess(List<ChargingStation> stations);
        void onError(String errorMessage);
    }

    public interface ChargingStationCallback {
        void onSuccess(ChargingStation station);
        void onError(String errorMessage);
    }

    /**
     * Get all charging stations
     */
    public void getAllChargingStations(ChargingStationsCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call = 
                stationApiService.getAllChargingStations("Bearer " + authToken);
        
        call.enqueue(new Callback<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call, 
                                 Response<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<ChargingStation> stations = convertToStationList(apiResponse.getData());
                        callback.onSuccess(stations);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get charging stations: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call, Throwable t) {
                Log.e(TAG, "Get all charging stations failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get charging station by ID
     */
    public void getChargingStationById(String stationId, ChargingStationCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<ChargingStationApiService.ChargingStationResponseDTO>> call = 
                stationApiService.getChargingStationById("Bearer " + authToken, stationId);
        
        call.enqueue(new Callback<ApiResponse<ChargingStationApiService.ChargingStationResponseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChargingStationApiService.ChargingStationResponseDTO>> call, 
                                 Response<ApiResponse<ChargingStationApiService.ChargingStationResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChargingStationApiService.ChargingStationResponseDTO> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ChargingStation station = convertToStation(apiResponse.getData());
                        callback.onSuccess(station);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get charging station: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChargingStationApiService.ChargingStationResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Get charging station by ID failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get nearby charging stations based on location
     */
    public void getNearbyStations(double latitude, double longitude, ChargingStationsCallback callback) {
        getNearbyStations(latitude, longitude, 10.0, callback); // Default 10km radius
    }

    /**
     * Get nearby charging stations with custom radius
     */
    public void getNearbyStations(double latitude, double longitude, double radiusKm, 
                                ChargingStationsCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        ChargingStationApiService.NearbyStationsRequest request = 
                new ChargingStationApiService.NearbyStationsRequest(latitude, longitude, radiusKm);
        
        Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call = 
                stationApiService.getNearbyStations("Bearer " + authToken, request);
        
        call.enqueue(new Callback<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call, 
                                 Response<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<ChargingStation> stations = convertToStationList(apiResponse.getData());
                        callback.onSuccess(stations);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get nearby stations: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChargingStationApiService.ChargingStationResponseDTO>>> call, Throwable t) {
                Log.e(TAG, "Get nearby stations failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get active charging stations only
     */
    public void getActiveChargingStations(ChargingStationsCallback callback) {
        getAllChargingStations(new ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                // Filter only active stations
                List<ChargingStation> activeStations = stations.stream()
                    .filter(ChargingStation::isActive)
                    .collect(java.util.stream.Collectors.toList());
                callback.onSuccess(activeStations);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get stations with available slots
     */
    public void getAvailableChargingStations(ChargingStationsCallback callback) {
        getAllChargingStations(new ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                // Filter stations with available slots
                List<ChargingStation> availableStations = stations.stream()
                    .filter(station -> station.isActive() && station.getAvailableSlots() > 0)
                    .collect(java.util.stream.Collectors.toList());
                callback.onSuccess(availableStations);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Search stations by name or location
     */
    public void searchStations(String query, ChargingStationsCallback callback) {
        getAllChargingStations(new ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                String lowerQuery = query.toLowerCase().trim();
                List<ChargingStation> filteredStations = stations.stream()
                    .filter(station -> 
                        station.getName().toLowerCase().contains(lowerQuery) ||
                        (station.getLocation().getAddress() != null && 
                         station.getLocation().getAddress().toLowerCase().contains(lowerQuery)) ||
                        (station.getLocation().getCity() != null && 
                         station.getLocation().getCity().toLowerCase().contains(lowerQuery)))
                    .collect(java.util.stream.Collectors.toList());
                callback.onSuccess(filteredStations);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Helper method to get auth token
     */
    private String getAuthToken() {
        return preferenceManager.getAccessToken();
    }

    /**
     * Convert ChargingStationResponseDTO list to ChargingStation list
     */
    private List<ChargingStation> convertToStationList(List<ChargingStationApiService.ChargingStationResponseDTO> dtoList) {
        List<ChargingStation> stations = new ArrayList<>();
        
        for (ChargingStationApiService.ChargingStationResponseDTO dto : dtoList) {
            ChargingStation station = convertToStation(dto);
            stations.add(station);
        }
        
        return stations;
    }

    /**
     * Convert ChargingStationResponseDTO to ChargingStation
     */
    private ChargingStation convertToStation(ChargingStationApiService.ChargingStationResponseDTO dto) {
        ChargingStation station = new ChargingStation();
        station.setId(dto.getId());
        station.setName(dto.getName());
        station.setPricePerHour(dto.getPricePerHour());
        station.setTotalSlots(dto.getTotalSlots());
        station.setAvailableSlots(dto.getAvailableSlots());
        station.setActive(dto.isActive());
        station.setAmenities(dto.getAmenities());
        
        // Convert type (integer to string)
        station.setType(String.valueOf(dto.getType()));
        
        // Create and set location data from nested DTO
        if (dto.getLocation() != null) {
            ChargingStation.Location location = new ChargingStation.Location();
            location.setLatitude(dto.getLocation().getLatitude());
            location.setLongitude(dto.getLocation().getLongitude());
            location.setAddress(dto.getLocation().getAddress());
            location.setCity(dto.getLocation().getCity());
            location.setProvince(dto.getLocation().getProvince());
            station.setLocation(location);
        }
        
        // Convert operating hours from nested DTO
        if (dto.getOperatingHours() != null) {
            ChargingStation.OperatingHours operatingHours = new ChargingStation.OperatingHours();
            operatingHours.setOpenTime(dto.getOperatingHours().getOpenTime());
            operatingHours.setCloseTime(dto.getOperatingHours().getCloseTime());
            operatingHours.setOperatingDays(dto.getOperatingHours().getOperatingDays());
            station.setOperatingHours(operatingHours);
        }
        
        // Set distance if available (for nearby search results)
        if (dto.getDistanceKm() > 0) {
            station.setDistance(dto.getDistanceKm());
        }
        
        return station;
    }
}
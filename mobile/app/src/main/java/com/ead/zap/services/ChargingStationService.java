package com.ead.zap.services;

import android.content.Context;

import com.ead.zap.models.ChargingStation;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.models.requests.NearbyStationsRequest;
import com.ead.zap.network.NetworkClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service class for handling charging station operations
 */
public class ChargingStationService {
    private final ChargingStationAPI chargingStationAPI;

    // Constructor
    public ChargingStationService(Context context) {
        this.chargingStationAPI = NetworkClient.getInstance(context).createService(ChargingStationAPI.class);
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
        Call<ApiResponse<List<ChargingStation>>> call = chargingStationAPI.getAllChargingStations();
        
        call.enqueue(new Callback<ApiResponse<List<ChargingStation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChargingStation>>> call, 
                                 Response<ApiResponse<List<ChargingStation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChargingStation>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get charging stations: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChargingStation>>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get charging station by ID
     */
    public void getChargingStationById(String stationId, ChargingStationCallback callback) {
        Call<ApiResponse<ChargingStation>> call = chargingStationAPI.getChargingStationById(stationId);
        
        call.enqueue(new Callback<ApiResponse<ChargingStation>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChargingStation>> call, 
                                 Response<ApiResponse<ChargingStation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChargingStation> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get charging station: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChargingStation>> call, Throwable t) {
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
        NearbyStationsRequest request = new NearbyStationsRequest(latitude, longitude, radiusKm);
        Call<ApiResponse<List<ChargingStation>>> call = chargingStationAPI.getNearbyStations(request);
        
        call.enqueue(new Callback<ApiResponse<List<ChargingStation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChargingStation>>> call, 
                                 Response<ApiResponse<List<ChargingStation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChargingStation>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get nearby stations: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChargingStation>>> call, Throwable t) {
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
}
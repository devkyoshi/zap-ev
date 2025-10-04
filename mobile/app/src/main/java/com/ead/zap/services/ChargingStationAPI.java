package com.ead.zap.services;

import com.ead.zap.models.ChargingStation;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.models.requests.NearbyStationsRequest;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API interface for charging station endpoints
 */
public interface ChargingStationAPI {

    /**
     * Get all charging stations
     */
    @GET("chargingstations")
    Call<ApiResponse<List<ChargingStation>>> getAllChargingStations();

    /**
     * Get charging station by ID
     */
    @GET("chargingstations/{id}")
    Call<ApiResponse<ChargingStation>> getChargingStationById(@Path("id") String stationId);

    /**
     * Get nearby charging stations
     */
    @POST("chargingstations/nearby")
    Call<ApiResponse<List<ChargingStation>>> getNearbyStations(@Body NearbyStationsRequest request);
}
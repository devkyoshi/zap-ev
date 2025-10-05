package com.ead.zap.api.services;

import com.ead.zap.config.ApiConfig;
import com.ead.zap.models.common.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Charging Station API service interface using Retrofit
 * Contains all charging station-related endpoints
 */
public interface ChargingStationApiService {

    /**
     * Get all charging stations
     * GET /api/chargingstations
     */
    @GET(ApiConfig.ChargingStations.BASE)
    Call<ApiResponse<List<ChargingStationResponseDTO>>> getAllChargingStations(
        @Header("Authorization") String authToken
    );

    /**
     * Get charging station by ID
     * GET /api/chargingstations/{id}
     */
    @GET(ApiConfig.ChargingStations.BASE + "/{id}")
    Call<ApiResponse<ChargingStationResponseDTO>> getChargingStationById(
        @Header("Authorization") String authToken,
        @Path("id") String stationId
    );

    /**
     * Get nearby charging stations
     * POST /api/chargingstations/nearby
     */
    @POST(ApiConfig.ChargingStations.NEARBY)
    Call<ApiResponse<List<ChargingStationResponseDTO>>> getNearbyStations(
        @Header("Authorization") String authToken,
        @Body NearbyStationsRequest request
    );

    /**
     * DTOs for API requests and responses
     */
    class NearbyStationsRequest {
        private double latitude;
        private double longitude;
        private double radiusKm;

        public NearbyStationsRequest() {}

        public NearbyStationsRequest(double latitude, double longitude, double radiusKm) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radiusKm = radiusKm;
        }

        // Getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public double getRadiusKm() { return radiusKm; }
        public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }
    }

    class ChargingStationResponseDTO {
        private String id;
        private String name;
        private LocationDTO location;
        private int type; // Backend sends integer type
        private double pricePerHour;
        private int totalSlots;
        private int availableSlots;
        private boolean isActive;
        private OperatingHoursDTO operatingHours;
        private List<String> amenities;
        private double distanceKm; // Only populated in nearby search results

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocationDTO getLocation() { return location; }
        public void setLocation(LocationDTO location) { this.location = location; }

        public int getType() { return type; }
        public void setType(int type) { this.type = type; }

        public double getPricePerHour() { return pricePerHour; }
        public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

        public int getTotalSlots() { return totalSlots; }
        public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

        public int getAvailableSlots() { return availableSlots; }
        public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public OperatingHoursDTO getOperatingHours() { return operatingHours; }
        public void setOperatingHours(OperatingHoursDTO operatingHours) { this.operatingHours = operatingHours; }

        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }

        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    }

    class LocationDTO {
        private double latitude;
        private double longitude;
        private String address;
        private String city;
        private String province;

        // Getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
    }

    class OperatingHoursDTO {
        private String openTime;
        private String closeTime;
        private List<Integer> operatingDays;

        // Getters and setters
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }

        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

        public List<Integer> getOperatingDays() { return operatingDays; }
        public void setOperatingDays(List<Integer> operatingDays) { this.operatingDays = operatingDays; }
    }
}
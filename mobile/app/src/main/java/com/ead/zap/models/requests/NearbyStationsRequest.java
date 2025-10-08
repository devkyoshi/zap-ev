package com.ead.zap.models.requests;

import com.google.gson.annotations.SerializedName;

public class NearbyStationsRequest {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("radiusKm")
    private double radiusKm;

    // Default constructor
    public NearbyStationsRequest() {
        this.radiusKm = 10.0; // Default radius
    }

    // Constructor
    public NearbyStationsRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = 10.0; // Default radius
    }

    // Constructor with custom radius
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
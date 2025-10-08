package com.ead.zap.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChargingStation {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private Location location;

    @SerializedName("type")
    private String type;

    @SerializedName("totalSlots")
    private int totalSlots;

    @SerializedName("availableSlots")
    private int availableSlots;

    @SerializedName("pricePerHour")
    private double pricePerHour;

    @SerializedName("operatingHours")
    private OperatingHours operatingHours;

    @SerializedName("amenities")
    private List<String> amenities;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("distance")
    private Double distance; // This will be calculated for nearby searches

    // Default constructor
    public ChargingStation() {}

    // Constructor
    public ChargingStation(String id, String name, Location location, String type, 
                          int totalSlots, int availableSlots, double pricePerHour) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.pricePerHour = pricePerHour;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public OperatingHours getOperatingHours() { return operatingHours; }
    public void setOperatingHours(OperatingHours operatingHours) { this.operatingHours = operatingHours; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("Rs. %.2f/hour", pricePerHour);
    }

    public String getAvailabilityText() {
        return availableSlots + " slots available";
    }

    public String getDistanceText() {
        if (distance != null) {
            if (distance < 1.0) {
                return String.format("%.0f m away", distance * 1000);
            } else {
                return String.format("%.1f km away", distance);
            }
        }
        return "";
    }

    // Inner classes for nested objects
    public static class Location {
        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        @SerializedName("address")
        private String address;

        @SerializedName("city")
        private String city;

        @SerializedName("province")
        private String province;

        // Constructors
        public Location() {}

        public Location(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }

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

        public String getFullAddress() {
            StringBuilder sb = new StringBuilder();
            if (address != null && !address.isEmpty()) {
                sb.append(address);
            }
            if (city != null && !city.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(city);
            }
            if (province != null && !province.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(province);
            }
            return sb.toString();
        }
    }

    public static class OperatingHours {
        @SerializedName("openTime")
        private String openTime;

        @SerializedName("closeTime")
        private String closeTime;

        @SerializedName("operatingDays")
        private List<Integer> operatingDays;

        // Constructors
        public OperatingHours() {}

        public OperatingHours(String openTime, String closeTime, List<Integer> operatingDays) {
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.operatingDays = operatingDays;
        }

        // Getters and setters
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }

        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

        public List<Integer> getOperatingDays() { return operatingDays; }
        public void setOperatingDays(List<Integer> operatingDays) { this.operatingDays = operatingDays; }

        public String getHoursText() {
            if (openTime != null && closeTime != null) {
                return openTime + " - " + closeTime;
            }
            return "24/7";
        }

        public boolean isOpenNow() {
            // Basic implementation - you can enhance this with actual time checking
            return true;
        }
    }
}
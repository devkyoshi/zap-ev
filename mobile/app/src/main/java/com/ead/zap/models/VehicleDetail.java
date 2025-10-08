package com.ead.zap.models;

/**
 * VehicleDetail model to match backend structure
 * Represents vehicle information for EV owners
 */
public class VehicleDetail {
    private String make;
    private String model;
    private String licensePlate;
    private int year;

    // Constructors
    public VehicleDetail() {}

    public VehicleDetail(String make, String model, String licensePlate, int year) {
        this.make = make;
        this.model = model;
        this.licensePlate = licensePlate;
        this.year = year;
    }

    // Getters and Setters
    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    // Helper methods
    public String getVehicleDisplayName() {
        if (make != null && model != null && year > 0) {
            return year + " " + make + " " + model;
        } else if (make != null && model != null) {
            return make + " " + model;
        } else if (make != null) {
            return make;
        } else {
            return "Unknown Vehicle";
        }
    }

    public boolean isValid() {
        return make != null && !make.trim().isEmpty() && 
               model != null && !model.trim().isEmpty() &&
               licensePlate != null && !licensePlate.trim().isEmpty() &&
               year > 1900 && year <= 2030;
    }

    @Override
    public String toString() {
        return "VehicleDetail{" +
                "make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", year=" + year +
                '}';
    }
}
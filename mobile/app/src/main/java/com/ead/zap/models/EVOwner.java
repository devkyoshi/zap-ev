package com.ead.zap.models;

import java.util.Date;
import java.util.List;

/**
 * EVOwner model for electric vehicle owners
 */
public class EVOwner {
    private String id;
    private String nic;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean isActive;
    private List<VehicleDetail> vehicleDetails;
    private Date lastLogin;
    private Date createdAt;
    private Date updatedAt;

    public EVOwner() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<VehicleDetail> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<VehicleDetail> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Vehicle detail model
     */
    public static class VehicleDetail {
        private String make;
        private String model;
        private String licensePlate;
        private int year;

        public VehicleDetail() {}

        public VehicleDetail(String make, String model, String licensePlate, int year) {
            this.make = make;
            this.model = model;
            this.licensePlate = licensePlate;
            this.year = year;
        }

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

        /**
         * Get vehicle display name
         */
        public String getDisplayName() {
            return year + " " + make + " " + model + " (" + licensePlate + ")";
        }
    }
}
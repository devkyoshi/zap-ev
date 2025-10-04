package com.ead.zap.models.auth;

import java.util.List;

/**
 * Registration request model for EV Owners
 */
public class EVOwnerRegistrationRequest {
    private String nic;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private List<VehicleDetail> vehicleDetails;

    public EVOwnerRegistrationRequest() {}

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<VehicleDetail> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<VehicleDetail> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    /**
     * Vehicle detail nested class
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
    }
}
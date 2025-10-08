package com.ead.zap.models;

import java.util.List;

/**
 * ProfileUpdateRequest model for updating user profile
 * Contains fields that can be updated by the user
 */
public class ProfileUpdateRequest {
    private String nic;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<VehicleDetail> vehicleDetails;

    // Constructors
    public ProfileUpdateRequest() {}

    public ProfileUpdateRequest(String nic, String firstName, String lastName, 
                              String email, String phoneNumber, List<VehicleDetail> vehicleDetails) {
        this.nic = nic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.vehicleDetails = vehicleDetails;
    }

    // Getters and Setters
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

    public List<VehicleDetail> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<VehicleDetail> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    @Override
    public String toString() {
        return "ProfileUpdateRequest{" +
                "nic='" + nic + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", vehicleDetails=" + vehicleDetails +
                '}';
    }
}
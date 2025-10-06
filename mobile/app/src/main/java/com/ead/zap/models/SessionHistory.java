package com.ead.zap.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class SessionHistory {
    @SerializedName("bookingId")
    private String bookingId;
    
    @SerializedName("evOwnerName")
    private String evOwnerName;
    
    @SerializedName("evOwnerNIC")
    private String evOwnerNIC;
    
    @SerializedName("evOwnerPhone")
    private String evOwnerPhone;
    
    @SerializedName("chargingStationId")
    private String chargingStationId;
    
    @SerializedName("chargingStationName")
    private String chargingStationName;
    
    @SerializedName("reservationDateTime")
    private Date reservationDateTime;
    
    @SerializedName("durationMinutes")
    private int durationMinutes;
    
    @SerializedName("status")
    private BookingStatus status;
    
    @SerializedName("statusDisplayName")
    private String statusDisplayName;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("actualStartTime")
    private Date actualStartTime;
    
    @SerializedName("actualEndTime")
    private Date actualEndTime;
    
    @SerializedName("energyDelivered")
    private Double energyDelivered;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    @SerializedName("customerVehicles")
    private List<VehicleDetail> customerVehicles;

    // Constructors
    public SessionHistory() {}

    public SessionHistory(String bookingId, String evOwnerName, String evOwnerNIC, String evOwnerPhone,
                         String chargingStationId, String chargingStationName, Date reservationDateTime,
                         int durationMinutes, BookingStatus status, String statusDisplayName,
                         double totalAmount, Date actualStartTime, Date actualEndTime,
                         Double energyDelivered, String notes, Date createdAt,
                         List<VehicleDetail> customerVehicles) {
        this.bookingId = bookingId;
        this.evOwnerName = evOwnerName;
        this.evOwnerNIC = evOwnerNIC;
        this.evOwnerPhone = evOwnerPhone;
        this.chargingStationId = chargingStationId;
        this.chargingStationName = chargingStationName;
        this.reservationDateTime = reservationDateTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
        this.totalAmount = totalAmount;
        this.actualStartTime = actualStartTime;
        this.actualEndTime = actualEndTime;
        this.energyDelivered = energyDelivered;
        this.notes = notes;
        this.createdAt = createdAt;
        this.customerVehicles = customerVehicles;
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getEvOwnerName() { return evOwnerName; }
    public String getEvOwnerNIC() { return evOwnerNIC; }
    public String getEvOwnerPhone() { return evOwnerPhone; }
    public String getChargingStationId() { return chargingStationId; }
    public String getChargingStationName() { return chargingStationName; }
    public Date getReservationDateTime() { return reservationDateTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public BookingStatus getStatus() { return status; }
    public String getStatusDisplayName() { return statusDisplayName; }
    public double getTotalAmount() { return totalAmount; }
    public Date getActualStartTime() { return actualStartTime; }
    public Date getActualEndTime() { return actualEndTime; }
    public Double getEnergyDelivered() { return energyDelivered; }
    public String getNotes() { return notes; }
    public Date getCreatedAt() { return createdAt; }
    public List<VehicleDetail> getCustomerVehicles() { return customerVehicles; }

    // Setters
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setEvOwnerName(String evOwnerName) { this.evOwnerName = evOwnerName; }
    public void setEvOwnerNIC(String evOwnerNIC) { this.evOwnerNIC = evOwnerNIC; }
    public void setEvOwnerPhone(String evOwnerPhone) { this.evOwnerPhone = evOwnerPhone; }
    public void setChargingStationId(String chargingStationId) { this.chargingStationId = chargingStationId; }
    public void setChargingStationName(String chargingStationName) { this.chargingStationName = chargingStationName; }
    public void setReservationDateTime(Date reservationDateTime) { this.reservationDateTime = reservationDateTime; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setActualStartTime(Date actualStartTime) { this.actualStartTime = actualStartTime; }
    public void setActualEndTime(Date actualEndTime) { this.actualEndTime = actualEndTime; }
    public void setEnergyDelivered(Double energyDelivered) { this.energyDelivered = energyDelivered; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setCustomerVehicles(List<VehicleDetail> customerVehicles) { this.customerVehicles = customerVehicles; }
    
    // Helper methods
    public String getFormattedEnergyDelivered() {
        if (energyDelivered != null) {
            return String.format("%.1f kWh", energyDelivered);
        }
        return "N/A";
    }
    
    public String getFormattedTotalAmount() {
        return String.format("$%.2f", totalAmount);
    }
    
    public String getPrimaryVehicle() {
        if (customerVehicles != null && !customerVehicles.isEmpty()) {
            VehicleDetail vehicle = customerVehicles.get(0);
            return String.format("%s %s (%s)", vehicle.getMake(), vehicle.getModel(), vehicle.getLicensePlate());
        }
        return "No vehicle info";
    }
}
package com.ead.zap.ui.operator.models;

public class SessionHistoryItem {
    private String bookingId;
    private String customerName;
    private String stationId;
    private String slotNumber;
    private String date;
    private String timeRange;
    private String energyDelivered;
    private String status;
    
    // Store the full session data for detailed view
    private com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO fullSessionData;

    public SessionHistoryItem(String bookingId, String customerName, String stationId, String slotNumber, 
                             String date, String timeRange, String energyDelivered, String status) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.stationId = stationId;
        this.slotNumber = slotNumber;
        this.date = date;
        this.timeRange = timeRange;
        this.energyDelivered = energyDelivered;
        this.status = status;
    }
    
    public SessionHistoryItem(String bookingId, String customerName, String stationId, String slotNumber, 
                             String date, String timeRange, String energyDelivered, String status,
                             com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO fullSessionData) {
        this(bookingId, customerName, stationId, slotNumber, date, timeRange, energyDelivered, status);
        this.fullSessionData = fullSessionData;
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getStationId() { return stationId; }
    public String getSlotNumber() { return slotNumber; }
    public String getDate() { return date; }
    public String getTimeRange() { return timeRange; }
    public String getEnergyDelivered() { return energyDelivered; }
    public String getStatus() { return status; }

    public com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO getFullSessionData() { 
        return fullSessionData; 
    }

    // Setters
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    public void setDate(String date) { this.date = date; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public void setEnergyDelivered(String energyDelivered) { this.energyDelivered = energyDelivered; }
    public void setStatus(String status) { this.status = status; }
    public void setFullSessionData(com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO fullSessionData) { 
        this.fullSessionData = fullSessionData; 
    }
}
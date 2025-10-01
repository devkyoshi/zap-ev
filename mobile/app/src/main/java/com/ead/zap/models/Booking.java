package com.ead.zap.models;

import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    private String bookingId;
    private String userId;
    private String stationId;
    private String stationName;
    private String stationAddress;
    private Date reservationDate;
    private Date reservationTime;
    private int duration; // in minutes
    private double totalCost;
    private String status; // PENDING, APPROVED, CANCELLED, COMPLETED
    private String qrCode;
    private Date createdAt;
    private Date updatedAt;

    public Booking() {
        // Default constructor
    }

    public Booking(String userId, String stationId, String stationName,
                   String stationAddress, Date reservationDate, Date reservationTime,
                   int duration, double totalCost) {
        this.userId = userId;
        this.stationId = stationId;
        this.stationName = stationName;
        this.stationAddress = stationAddress;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.duration = duration;
        this.totalCost = totalCost;
        this.status = "PENDING";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationAddress() { return stationAddress; }
    public void setStationAddress(String stationAddress) { this.stationAddress = stationAddress; }

    public Date getReservationDate() { return reservationDate; }
    public void setReservationDate(Date reservationDate) { this.reservationDate = reservationDate; }

    public Date getReservationTime() { return reservationTime; }
    public void setReservationTime(Date reservationTime) { this.reservationTime = reservationTime; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods for display
    public String getDate() {
        if (reservationDate != null) {
            return android.text.format.DateFormat.format("MMM dd, yyyy", reservationDate).toString();
        }
        return "";
    }

    public String getTime() {
        if (reservationTime != null) {
            return android.text.format.DateFormat.format("hh:mm a", reservationTime).toString();
        }
        return "";
    }

    // Check if booking can be modified (at least 12 hours before)
    public boolean canBeModified() {
        if (reservationTime == null) return false;
        
        long currentTime = System.currentTimeMillis();
        long bookingTime = reservationTime.getTime();
        long twelveHoursInMs = 12 * 60 * 60 * 1000;
        
        return (bookingTime - currentTime) > twelveHoursInMs && 
               ("PENDING".equals(status) || "APPROVED".equals(status));
    }

    // Check if booking can be cancelled (at least 12 hours before)
    public boolean canBeCancelled() {
        return canBeModified(); // Same logic as modification
    }

    // Check if QR code can be shown (approved and within access window)
    public boolean canShowQRCode() {
        // For now, show QR code for all APPROVED bookings to make testing easier
        // In production, you can add time restrictions as needed
        return "APPROVED".equals(status);
        
        /* Production logic with time restrictions:
        if (!"APPROVED".equals(status)) return false;
        
        long currentTime = System.currentTimeMillis();
        long bookingTime = reservationTime.getTime();
        long thirtyMinutesInMs = 30 * 60 * 1000;
        long durationInMs = duration * 60 * 1000;
        
        // Show QR for approved bookings - either within access window or for future bookings
        // Access window: 30 minutes before until booking ends
        boolean withinAccessWindow = currentTime >= (bookingTime - thirtyMinutesInMs) && 
                                   currentTime <= (bookingTime + durationInMs);
        
        // For future bookings, show QR if booking is within 24 hours
        boolean futureBookingWithin24Hours = bookingTime > currentTime && 
                                            (bookingTime - currentTime) <= (24 * 60 * 60 * 1000);
        
        return withinAccessWindow || futureBookingWithin24Hours;
        */
    }
}
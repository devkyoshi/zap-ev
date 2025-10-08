package com.ead.zap.models;

/**
 * Booking status enum that matches the backend BookingStatus enum
 * Maps integer values from backend to display strings
 */
public enum BookingStatus {
    PENDING(1, "Pending"),
    APPROVED(2, "Approved"), 
    IN_PROGRESS(3, "In Progress"),
    COMPLETED(4, "Completed"),
    CANCELLED(5, "Cancelled"),
    NO_SHOW(6, "No Show");

    private final int value;
    private final String displayName;

    BookingStatus(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert integer value from backend to BookingStatus enum
     */
    public static BookingStatus fromValue(int value) {
        for (BookingStatus status : BookingStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return PENDING; // Default fallback
    }

    /**
     * Convert string status to BookingStatus enum (for backward compatibility)
     */
    public static BookingStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }

        // Handle numeric strings (from API)
        try {
            int numericValue = Integer.parseInt(statusString.trim());
            return fromValue(numericValue);
        } catch (NumberFormatException e) {
            // Not a number, try string matching
        }

        // Handle string values (case-insensitive)
        String upperStatus = statusString.toUpperCase().trim();
        switch (upperStatus) {
            case "PENDING":
                return PENDING;
            case "APPROVED":
                return APPROVED;
            case "IN_PROGRESS":
            case "INPROGRESS":
                return IN_PROGRESS;
            case "COMPLETED":
                return COMPLETED;
            case "CANCELLED":
            case "CANCELED":
                return CANCELLED;
            case "NO_SHOW":
            case "NOSHOW":
                return NO_SHOW;
            default:
                android.util.Log.w("BookingStatus", "Unknown status string: " + statusString + ", defaulting to PENDING");
                return PENDING;
        }
    }

    /**
     * Check if booking can be modified based on status
     */
    public boolean canBeModified() {
        return this == PENDING || this == APPROVED;
    }

    /**
     * Check if booking can be cancelled based on status
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == APPROVED;
    }

    /**
     * Check if QR code can be shown based on status
     */
    public boolean canShowQRCode() {
        return this == APPROVED || this == PENDING; // Allow QR for pending bookings in demo
    }

    /**
     * Get color resource ID for status display
     */
    public int getStatusColorRes() {
        switch (this) {
            case APPROVED:
                return com.ead.zap.R.color.primary_light; // Green
            case PENDING:
                return com.ead.zap.R.color.amber_600; // Amber
            case IN_PROGRESS:
                return com.ead.zap.R.color.blue_600; // Blue
            case COMPLETED:
                return com.ead.zap.R.color.gray_600; // Gray
            case CANCELLED:
            case NO_SHOW:
                return com.ead.zap.R.color.red_600; // Red
            default:
                return android.R.color.black;
        }
    }
}
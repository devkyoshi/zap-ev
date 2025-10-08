package com.ead.zap.ui.operator.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ead.zap.R;
import com.ead.zap.api.services.BookingApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionDetailDialog extends Dialog {
    
    private TextView tvCustomerName, tvCustomerNIC, tvCustomerPhone, tvVehicleInfo;
    private TextView tvStatus, tvStation, tvDate, tvTimeRange, tvDuration, tvEnergy, tvTotalAmount;
    private TextView tvNotes, tvBookingRef;
    private LinearLayout layoutNotes;
    private ImageView ivClose;

    public SessionDetailDialog(@NonNull Context context) {
        super(context, R.style.Theme_Dialog_Fullscreen);
        setContentView(R.layout.dialog_session_details);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Customer Information
        tvCustomerName = findViewById(R.id.tv_detail_customer_name);
        tvCustomerNIC = findViewById(R.id.tv_detail_customer_nic);
        tvCustomerPhone = findViewById(R.id.tv_detail_customer_phone);
        tvVehicleInfo = findViewById(R.id.tv_detail_vehicle_info);

        // Session Information
        tvStatus = findViewById(R.id.tv_detail_status);
        tvStation = findViewById(R.id.tv_detail_station);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTimeRange = findViewById(R.id.tv_detail_time_range);
        tvDuration = findViewById(R.id.tv_detail_duration);
        tvEnergy = findViewById(R.id.tv_detail_energy);
        tvTotalAmount = findViewById(R.id.tv_detail_total_amount);

        // Notes and Reference
        tvNotes = findViewById(R.id.tv_detail_notes);
        tvBookingRef = findViewById(R.id.tv_detail_booking_ref);
        layoutNotes = findViewById(R.id.layout_notes);

        // Controls
        ivClose = findViewById(R.id.iv_close);
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        
        // Allow dismissing by clicking outside (optional)
        setCanceledOnTouchOutside(true);
    }

    public void showSessionDetails(BookingApiService.SessionHistoryResponseDTO sessionHistory) {
        if (sessionHistory == null) {
            return;
        }

        try {
            // Customer Information
            String customerName = sessionHistory.getEvOwnerName();
            if (customerName == null || customerName.trim().isEmpty() || "Unknown Customer".equals(customerName)) {
                customerName = "Customer (" + sessionHistory.getEvOwnerNIC().substring(0, Math.min(4, sessionHistory.getEvOwnerNIC().length())) + "***)";
            }
            tvCustomerName.setText(customerName);
            tvCustomerNIC.setText(sessionHistory.getEvOwnerNIC());
            tvCustomerPhone.setText(sessionHistory.getEvOwnerPhone() != null ? sessionHistory.getEvOwnerPhone() : "N/A");

            // Vehicle Information
            String vehicleInfo = "No vehicle information";
            if (sessionHistory.getCustomerVehicles() != null && !sessionHistory.getCustomerVehicles().isEmpty()) {
                BookingApiService.VehicleDetailDTO vehicle = sessionHistory.getCustomerVehicles().get(0);
                if (vehicle.getMake() != null && !vehicle.getMake().isEmpty()) {
                    vehicleInfo = String.format("%s %s (%d)\nLicense: %s", 
                        vehicle.getMake(), 
                        vehicle.getModel(), 
                        vehicle.getYear(),
                        vehicle.getLicensePlate());
                }
            }
            tvVehicleInfo.setText(vehicleInfo);

            // Session Information
            String status = sessionHistory.getStatusDisplayName();
            if (status == null || status.trim().isEmpty()) {
                status = formatStatusName(sessionHistory.getStatus());
            }
            tvStatus.setText(status);

            String stationName = sessionHistory.getChargingStationName();
            if (stationName == null || stationName.trim().isEmpty()) {
                stationName = "Station " + sessionHistory.getChargingStationId();
            }
            tvStation.setText(stationName);

            // Date and Time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            String dateText = "N/A";
            String timeRangeText = "N/A";

            // Try to use actual times first, then reservation time
            if (sessionHistory.getActualStartTime() != null && !sessionHistory.getActualStartTime().isEmpty() &&
                sessionHistory.getActualEndTime() != null && !sessionHistory.getActualEndTime().isEmpty()) {
                
                Date startDate = parseISODate(sessionHistory.getActualStartTime());
                Date endDate = parseISODate(sessionHistory.getActualEndTime());
                
                if (startDate != null && endDate != null) {
                    dateText = dateFormat.format(startDate);
                    timeRangeText = timeFormat.format(startDate) + " - " + timeFormat.format(endDate);
                }
            } else if (sessionHistory.getReservationDateTime() != null && !sessionHistory.getReservationDateTime().isEmpty()) {
                Date reservationDate = parseISODate(sessionHistory.getReservationDateTime());
                if (reservationDate != null) {
                    dateText = dateFormat.format(reservationDate);
                    long endTimeMillis = reservationDate.getTime() + (sessionHistory.getDurationMinutes() * 60000L);
                    Date endDate = new Date(endTimeMillis);
                    timeRangeText = timeFormat.format(reservationDate) + " - " + timeFormat.format(endDate) + " (Scheduled)";
                }
            }

            tvDate.setText(dateText);
            tvTimeRange.setText(timeRangeText);

            // Duration
            int durationMinutes = sessionHistory.getDurationMinutes();
            String durationText = formatDuration(durationMinutes);
            tvDuration.setText(durationText);

            // Energy
            String energyText = "N/A";
            if (sessionHistory.getEnergyDelivered() != null && sessionHistory.getEnergyDelivered() > 0) {
                energyText = String.format("%.1f kWh", sessionHistory.getEnergyDelivered());
            } else {
                // Calculate estimated energy
                double estimatedEnergy = (durationMinutes / 60.0) * 7.5; // 7.5 kWh per hour
                energyText = String.format("%.1f kWh (Est.)", estimatedEnergy);
            }
            tvEnergy.setText(energyText);

            // Total Amount
            tvTotalAmount.setText(String.format("$%.2f", sessionHistory.getTotalAmount()));

            // Notes
            if (sessionHistory.getNotes() != null && !sessionHistory.getNotes().trim().isEmpty()) {
                tvNotes.setText(sessionHistory.getNotes());
                layoutNotes.setVisibility(View.VISIBLE);
            } else {
                layoutNotes.setVisibility(View.GONE);
            }

            // Booking Reference
            tvBookingRef.setText("Booking Reference: " + sessionHistory.getBookingId());

        } catch (Exception e) {
            android.util.Log.e("SessionDetailDialog", "Error displaying session details", e);
            // Show error state or dismiss
        }

        // Show the dialog
        show();
    }

    private Date parseISODate(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return null;
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            return isoFormat.parse(isoDateString);
        } catch (Exception e) {
            try {
                SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                return fallbackFormat.parse(isoDateString);
            } catch (Exception e2) {
                android.util.Log.w("SessionDetailDialog", "Failed to parse date: " + isoDateString, e2);
                return null;
            }
        }
    }

    private String formatStatusName(String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }
        
        // Try to parse as numeric first
        try {
            int numericStatus = Integer.parseInt(status.trim());
            switch (numericStatus) {
                case 1: return "Pending";
                case 2: return "Approved";
                case 3: return "In Progress";
                case 4: return "Completed";
                case 5: return "Cancelled";
                case 6: return "No Show";
                default: return "Unknown";
            }
        } catch (NumberFormatException e) {
            // Handle string status
            return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        }
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "N/A";
        }
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (hours > 0 && remainingMinutes > 0) {
            return String.format("%d hour%s %d minute%s", 
                hours, hours == 1 ? "" : "s",
                remainingMinutes, remainingMinutes == 1 ? "" : "s");
        } else if (hours > 0) {
            return String.format("%d hour%s", hours, hours == 1 ? "" : "s");
        } else {
            return String.format("%d minute%s", remainingMinutes, remainingMinutes == 1 ? "" : "s");
        }
    }
}
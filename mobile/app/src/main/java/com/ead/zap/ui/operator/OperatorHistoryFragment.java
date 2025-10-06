package com.ead.zap.ui.operator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.ead.zap.R;
import com.ead.zap.api.services.BookingApiService;
import com.ead.zap.models.Booking;
import com.ead.zap.models.BookingStatus;
import com.ead.zap.services.OperatorService;
import com.ead.zap.ui.operator.adapters.SessionHistoryAdapter;
import com.ead.zap.ui.operator.dialogs.SessionDetailDialog;
import com.ead.zap.ui.operator.models.SessionHistoryItem;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class OperatorHistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private SessionHistoryAdapter adapter;
    private List<SessionHistoryItem> historyItems;
    private OperatorService operatorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operator_history, container, false);
        
        // Initialize services
        operatorService = new OperatorService(requireContext());
        
        initViews(view);
        setupRecyclerView();
        loadHistoryData();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewHistory = view.findViewById(R.id.recycler_view_history);
    }

    private void setupRecyclerView() {
        historyItems = new ArrayList<>();
        adapter = new SessionHistoryAdapter(historyItems);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(adapter);
        
        // Set click listener for showing session details
        adapter.setOnSessionClickListener(sessionItem -> {
            if (sessionItem.getFullSessionData() != null) {
                showSessionDetailDialog(sessionItem.getFullSessionData());
            }
        });
    }

    private void loadHistoryData() {
        Log.d("OperatorHistory", "Starting to load session history...");

        operatorService.getSessionHistory(new OperatorService.SessionHistoryCallback() {
            @Override
            public void onSuccess(List<BookingApiService.SessionHistoryResponseDTO> sessionHistoryList) {
                Log.d("OperatorHistory", "API call successful. Retrieved " + sessionHistoryList.size() + " session history items");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        historyItems.clear();
                        
                        // Convert SessionHistoryResponseDTO objects to SessionHistoryItem objects
                        for (BookingApiService.SessionHistoryResponseDTO sessionHistory : sessionHistoryList) {
                            Log.d("OperatorHistory", "Processing session: " + sessionHistory.getBookingId() + 
                                  " Customer: " + sessionHistory.getEvOwnerName() + 
                                  " Status: " + sessionHistory.getStatusDisplayName());
                            
                            SessionHistoryItem item = convertSessionHistoryToItem(sessionHistory);
                            historyItems.add(item);
                            Log.d("OperatorHistory", "Added session: " + sessionHistory.getBookingId());
                        }
                        
                        Log.d("OperatorHistory", "Total sessions found: " + historyItems.size());
                        adapter.notifyDataSetChanged();
                        
                        if (historyItems.isEmpty()) {
                            Toast.makeText(getContext(), "No session history found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Loaded " + historyItems.size() + " sessions", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("OperatorHistory", "Failed to load session history: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading history: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private SessionHistoryItem convertSessionHistoryToItem(BookingApiService.SessionHistoryResponseDTO sessionHistory) {
        // Debug logging to see what data we're receiving
        Log.d("OperatorHistory", "Converting session data:");
        Log.d("OperatorHistory", "  BookingId: " + sessionHistory.getBookingId());
        Log.d("OperatorHistory", "  CustomerName: " + sessionHistory.getEvOwnerName());
        Log.d("OperatorHistory", "  Status: " + sessionHistory.getStatus());
        Log.d("OperatorHistory", "  StatusDisplayName: " + sessionHistory.getStatusDisplayName());
        Log.d("OperatorHistory", "  ReservationDateTime: " + sessionHistory.getReservationDateTime());
        Log.d("OperatorHistory", "  ActualStartTime: " + sessionHistory.getActualStartTime());
        Log.d("OperatorHistory", "  ActualEndTime: " + sessionHistory.getActualEndTime());
        Log.d("OperatorHistory", "  EnergyDelivered: " + sessionHistory.getEnergyDelivered());
        Log.d("OperatorHistory", "  DurationMinutes: " + sessionHistory.getDurationMinutes());
        
        // Format dates and times
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        
        String date = "N/A";
        String timeRange = "N/A";
        
        try {
            Date actualStartDate = null;
            Date actualEndDate = null;
            Date reservationDate = null;
            
            // Parse actual start and end times first (preferred for completed sessions)
            if (sessionHistory.getActualStartTime() != null && !sessionHistory.getActualStartTime().isEmpty() &&
                sessionHistory.getActualEndTime() != null && !sessionHistory.getActualEndTime().isEmpty()) {
                
                actualStartDate = parseISODate(sessionHistory.getActualStartTime());
                actualEndDate = parseISODate(sessionHistory.getActualEndTime());
                
                if (actualStartDate != null && actualEndDate != null) {
                    date = dateFormat.format(actualStartDate);
                    String startTime = timeFormat.format(actualStartDate);
                    String endTime = timeFormat.format(actualEndDate);
                    timeRange = startTime + " - " + endTime;
                }
            }
            
            // Fallback to reservation time if no actual times available
            if ((actualStartDate == null || actualEndDate == null) && 
                sessionHistory.getReservationDateTime() != null && !sessionHistory.getReservationDateTime().isEmpty()) {
                
                reservationDate = parseISODate(sessionHistory.getReservationDateTime());
                if (reservationDate != null) {
                    date = dateFormat.format(reservationDate);
                    String startTime = timeFormat.format(reservationDate);
                    
                    // Calculate expected end time based on duration
                    long endTimeMillis = reservationDate.getTime() + (sessionHistory.getDurationMinutes() * 60000L);
                    Date endDate = new Date(endTimeMillis);
                    String endTime = timeFormat.format(endDate);
                    timeRange = startTime + " - " + endTime + " (Scheduled)";
                }
            }
            
        } catch (Exception e) {
            Log.w("OperatorHistory", "Error parsing date/time for session: " + sessionHistory.getBookingId(), e);
        }
        
        // Get energy delivered with proper calculation
        String energy = "N/A";
        if (sessionHistory.getEnergyDelivered() != null && sessionHistory.getEnergyDelivered() > 0) {
            energy = String.format("%.1f kWh", sessionHistory.getEnergyDelivered());
        } else {
            // Calculate based on actual duration if available
            try {
                if (sessionHistory.getActualStartTime() != null && sessionHistory.getActualEndTime() != null &&
                    !sessionHistory.getActualStartTime().isEmpty() && !sessionHistory.getActualEndTime().isEmpty()) {
                    
                    Date startDate = parseISODate(sessionHistory.getActualStartTime());
                    Date endDate = parseISODate(sessionHistory.getActualEndTime());
                    
                    if (startDate != null && endDate != null) {
                        double actualHours = (endDate.getTime() - startDate.getTime()) / (1000.0 * 60 * 60);
                        double calculatedEnergy = actualHours * 7.5; // 7.5 kWh per hour
                        energy = String.format("%.1f kWh", calculatedEnergy);
                    }
                } else if (sessionHistory.getDurationMinutes() > 0) {
                    // Fallback to scheduled duration
                    double scheduledHours = sessionHistory.getDurationMinutes() / 60.0;
                    double calculatedEnergy = scheduledHours * 7.5;
                    energy = String.format("%.1f kWh (Est.)", calculatedEnergy);
                }
            } catch (Exception e) {
                Log.w("OperatorHistory", "Error calculating energy for session: " + sessionHistory.getBookingId(), e);
                energy = "N/A";
            }
        }
        
        // Use the proper customer name and add vehicle info
        String customerName = sessionHistory.getEvOwnerName();
        if (customerName == null || customerName.trim().isEmpty() || "Unknown Customer".equals(customerName)) {
            customerName = "Customer (" + sessionHistory.getEvOwnerNIC().substring(0, Math.min(4, sessionHistory.getEvOwnerNIC().length())) + "***)";
        }
        
        // Add vehicle info to customer name if available
        if (sessionHistory.getCustomerVehicles() != null && !sessionHistory.getCustomerVehicles().isEmpty()) {
            BookingApiService.VehicleDetailDTO vehicle = sessionHistory.getCustomerVehicles().get(0);
            if (vehicle.getMake() != null && !vehicle.getMake().isEmpty()) {
                customerName += "\n" + vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getLicensePlate() + ")";
            }
        }
        
        String stationInfo = sessionHistory.getChargingStationName() != null ? 
            sessionHistory.getChargingStationName() : 
            "Station " + sessionHistory.getChargingStationId();
        
        // Fix status display
        String displayStatus = "Unknown";
        if (sessionHistory.getStatusDisplayName() != null && !sessionHistory.getStatusDisplayName().trim().isEmpty()) {
            displayStatus = sessionHistory.getStatusDisplayName();
        } else if (sessionHistory.getStatus() != null && !sessionHistory.getStatus().trim().isEmpty()) {
            displayStatus = formatStatusName(sessionHistory.getStatus());
        }
        
        return new SessionHistoryItem(
                sessionHistory.getBookingId(),
                customerName,
                stationInfo,
                "A1", // Default slot - this could be enhanced if slot info is available
                date,
                timeRange,
                energy,
                displayStatus,
                sessionHistory // Pass the full session data for detailed view
        );
    }

    /**
     * Parse ISO 8601 date string to Date object
     */
    private Date parseISODate(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing with different formats
            // Format 1: Full ISO with timezone (e.g., "2025-10-06T14:30:00.000Z")
            if (isoDateString.contains("T")) {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                return isoFormat.parse(isoDateString);
            }
            
            // Format 2: Simple datetime (e.g., "2025-10-06 14:30:00")
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return simpleFormat.parse(isoDateString);
            
        } catch (ParseException e) {
            Log.w("OperatorHistory", "Failed to parse date: " + isoDateString, e);
            try {
                // Fallback: try without milliseconds
                SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                return fallbackFormat.parse(isoDateString);
            } catch (ParseException e2) {
                Log.w("OperatorHistory", "Fallback date parsing also failed: " + isoDateString, e2);
                return null;
            }
        }
    }

    /**
     * Format status name for display - handles both numeric and string values
     */
    private String formatStatusName(String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }
        
        // Try to parse as numeric first (backend sends numbers)
        try {
            int numericStatus = Integer.parseInt(status.trim());
            BookingStatus bookingStatus = BookingStatus.fromValue(numericStatus);
            return bookingStatus.getDisplayName();
        } catch (NumberFormatException e) {
            // Not a number, handle as string
            Log.d("OperatorHistory", "Status not numeric, parsing as string: " + status);
        }
        
        // Use the BookingStatus enum's string parsing
        BookingStatus bookingStatus = BookingStatus.fromString(status);
        return bookingStatus.getDisplayName();
    }

    /**
     * Show session detail dialog
     */
    private void showSessionDetailDialog(BookingApiService.SessionHistoryResponseDTO sessionData) {
        if (getContext() != null) {
            SessionDetailDialog dialog = new SessionDetailDialog(getContext());
            dialog.showSessionDetails(sessionData);
        }
    }


}
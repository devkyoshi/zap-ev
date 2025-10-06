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
import com.ead.zap.models.Booking;
import com.ead.zap.services.OperatorService;
import com.ead.zap.ui.operator.adapters.SessionHistoryAdapter;
import com.ead.zap.ui.operator.models.SessionHistoryItem;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    }

    private void loadHistoryData() {
        Log.d("OperatorHistory", "Starting to load session history...");

        operatorService.getSessionHistory(new OperatorService.BookingHistoryCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                Log.d("OperatorHistory", "API call successful. Retrieved " + bookings.size() + " bookings");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        historyItems.clear();
                        
                        // Convert Booking objects to SessionHistoryItem objects
                        for (Booking booking : bookings) {
                            // Log booking processing
                            if (booking.getStatus() == null) {
                                Log.w("OperatorHistory", "Booking " + booking.getBookingId() + " has null status");
                            } else {
                                Log.d("OperatorHistory", "Processing booking " + booking.getBookingId() + 
                                      " with status: " + booking.getStatus().name() + 
                                      " (value: " + booking.getStatus().getValue() + ")");
                            }
                            
                            String statusStr = "null";
                            try {
                                statusStr = booking.getStatus() != null ? booking.getStatusString() : "null";
                            } catch (Exception e) {
                                Log.w("OperatorHistory", "Error getting status string for booking " + booking.getBookingId(), e);
                                statusStr = "unknown";
                            }
                            
                            Log.d("OperatorHistory", "Processing booking " + booking.getBookingId() + 
                                  " with status: " + (booking.getStatus() != null ? booking.getStatus().name() : "null") + 
                                  " statusString: " + statusStr);
                            
                            // Check for completed status
                            boolean isCompleted = booking.getStatus() != null && 
                                                 booking.getStatus() == com.ead.zap.models.BookingStatus.COMPLETED;
                            
                            Log.d("OperatorHistory", "Booking " + booking.getBookingId() + 
                                  " is completed: " + isCompleted);
                            
                            if (isCompleted) {
                                SessionHistoryItem item = convertBookingToHistoryItem(booking);
                                historyItems.add(item);
                                Log.d("OperatorHistory", "Added completed session: " + booking.getBookingId());
                            }
                        }
                        
                        Log.d("OperatorHistory", "Total completed sessions found: " + historyItems.size());
                        adapter.notifyDataSetChanged();
                        
                        if (historyItems.isEmpty()) {
                            Toast.makeText(getContext(), "No completed sessions found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Loaded " + historyItems.size() + " completed sessions", Toast.LENGTH_SHORT).show();
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

    private SessionHistoryItem convertBookingToHistoryItem(Booking booking) {
        // Format dates and times
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        
        String date = "N/A";
        String timeRange = "N/A";
        
        try {
            // Use the built-in helper methods from Booking model
            date = booking.getDate();
            String startTime = booking.getTime();
            
            // Calculate end time based on duration
            if (booking.getReservationTime() != null) {
                Date endDate = new Date(booking.getReservationTime().getTime() + (booking.getDuration() * 60000L));
                String endTime = timeFormat.format(endDate);
                timeRange = startTime + " - " + endTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Calculate energy delivered (mock calculation based on duration)
        double energyDelivered = booking.getDuration() / 60.0 * 7.5; // 7.5 kWh per hour
        String energy = String.format("%.1f kWh", energyDelivered);
        
        // Safely get status string
        String statusString = "Unknown";
        try {
            if (booking.getStatus() != null) {
                statusString = booking.getStatusString();
            }
        } catch (Exception e) {
            Log.w("OperatorHistory", "Error getting status string in convertBookingToHistoryItem", e);
            statusString = "Status Error";
        }
        
        return new SessionHistoryItem(
                booking.getBookingId(),
                booking.getUserId(), // Using user ID as customer name for now
                booking.getStationId(),
                "A1", // Default slot
                date,
                timeRange,
                energy,
                statusString
        );
    }


}
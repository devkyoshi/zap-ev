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

        operatorService.getSessionHistory(new OperatorService.BookingHistoryCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        historyItems.clear();
                        
                        // Convert Booking objects to SessionHistoryItem objects
                        for (Booking booking : bookings) {
                            if ("COMPLETED".equals(booking.getStatus())) {
                                SessionHistoryItem item = convertBookingToHistoryItem(booking);
                                historyItems.add(item);
                            }
                        }
                        
                        adapter.notifyDataSetChanged();
                        
                        if (historyItems.isEmpty()) {
                            Toast.makeText(getContext(), "No completed sessions found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading history: " + error, Toast.LENGTH_LONG).show();
                        
                        // Load mock data as fallback
                        loadMockData();
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
        
        return new SessionHistoryItem(
                booking.getBookingId(),
                booking.getUserId(), // Using user ID as customer name for now
                booking.getStationId(),
                "A1", // Default slot
                date,
                timeRange,
                energy,
                booking.getStatusString()
        );
    }

    private void loadMockData() {
        // Fallback mock data for demonstration
        historyItems.clear();
        historyItems.add(new SessionHistoryItem("BK12345", "John Doe", "ST001", "A1", "Oct 2, 2025", "2:00 PM - 4:00 PM", "5.2 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12344", "Jane Smith", "ST001", "B2", "Oct 2, 2025", "12:00 PM - 1:30 PM", "3.8 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12343", "Mike Johnson", "ST002", "A1", "Oct 1, 2025", "3:00 PM - 5:00 PM", "6.1 kWh", "Completed"));
        adapter.notifyDataSetChanged();
    }
}
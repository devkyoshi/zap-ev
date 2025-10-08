package com.ead.zap.ui.operator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.api.services.BookingApiService;
import com.ead.zap.models.Booking;
import com.ead.zap.services.OperatorService;

public class OperatorQRScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final String TAG = "OperatorQRScannerFragment";
    
    private Button btnScanQR, btnRefreshStatus, btnViewHistory;
    private TextView tvWelcome, tvInstructions, tvActiveSessions, tvAvailableSlots;
    private OperatorService operatorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operator_qr_scanner, container, false);
        
        operatorService = new OperatorService(requireContext());
        initViews(view);
        setupClickListeners();
        loadStationStatus();
        
        return view;
    }

    private void initViews(View view) {
        btnScanQR = view.findViewById(R.id.btn_scan_qr);
        btnRefreshStatus = view.findViewById(R.id.btn_refresh_status);
        btnViewHistory = view.findViewById(R.id.btn_view_history);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvInstructions = view.findViewById(R.id.tv_instructions);
        tvActiveSessions = view.findViewById(R.id.tv_active_sessions);
        tvAvailableSlots = view.findViewById(R.id.tv_available_slots);
    }

    private void setupClickListeners() {
        btnScanQR.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openQRScanner();
            } else {
                requestCameraPermission();
            }
        });
        
        btnRefreshStatus.setOnClickListener(v -> {
            loadStationStatus();
            Toast.makeText(getContext(), "Status refreshed", Toast.LENGTH_SHORT).show();
        });
        
        btnViewHistory.setOnClickListener(v -> {
            // Navigate to history tab by triggering bottom navigation
            if (getActivity() instanceof com.ead.zap.ui.operator.StationOperatorMain) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                        getActivity().findViewById(R.id.bottom_navigation_operator);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.navigation_history);
                }
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }

    private void openQRScanner() {
        // Open the real QR scanner activity
        Intent intent = new Intent(getActivity(), com.ead.zap.ui.operator.modals.QRScannerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openQRScanner();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadStationStatus() {
        // Load session history to calculate active sessions
        operatorService.getSessionHistory(new OperatorService.SessionHistoryCallback() {
            @Override
            public void onSuccess(java.util.List<com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO> sessions) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Count active sessions (status = IN_PROGRESS)
                        int activeSessions = 0;
                        for (com.ead.zap.api.services.BookingApiService.SessionHistoryResponseDTO session : sessions) {
                            // Count sessions that are currently active/in progress
                            if (session.getStatus() != null && 
                                (session.getStatus().equals("InProgress") || 
                                 session.getStatus().equals("IN_PROGRESS") ||
                                 session.getStatusDisplayName() != null && 
                                 session.getStatusDisplayName().equals("In Progress"))) {
                                activeSessions++;
                            }
                        }
                        
                        // Update UI with real data
                        tvActiveSessions.setText(String.valueOf(activeSessions));
                        
                        // Calculate available slots (assuming 5 total slots as example)
                        int totalSlots = 5;
                        int availableSlots = Math.max(0, totalSlots - activeSessions);
                        tvAvailableSlots.setText(String.valueOf(availableSlots));
                        
                        Log.d(TAG, "Station status updated: " + activeSessions + " active, " + availableSlots + " available");
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Failed to load station status: " + error);
                        // Show fallback values
                        tvActiveSessions.setText("0");
                        tvAvailableSlots.setText("5");
                    });
                }
            }
        });
    }
}
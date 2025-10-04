package com.ead.zap.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.LocationService;
import com.ead.zap.services.ProfileService;
import com.ead.zap.ui.owner.modals.CreateBookingActivity;
import com.ead.zap.utils.PreferenceManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OwnerHomeFragment extends Fragment {
    private static final String TAG = "OwnerHomeFragment";

    private TextView tvWelcomeText, tvPendingCount, tvApprovedCount, 
                    tvUpcomingReservation, tvNearbyStationsCount;
    private MaterialCardView cardQuickCharge, cardHistory, cardUpcomingReservation;
    
    // Services
    private ProfileService profileService;
    private PreferenceManager preferenceManager;
    private ChargingStationService chargingStationService;
    private LocationService locationService;

    public OwnerHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            initServices();
            initViews(view);
            setupClickListeners();
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initServices() {
        if (getContext() != null) {
            profileService = new ProfileService(getContext());
            preferenceManager = new PreferenceManager(getContext());
            chargingStationService = new ChargingStationService(getContext());
            locationService = new LocationService(getContext());
        }
    }

    private void initViews(View view) {
        try {
            tvWelcomeText = view.findViewById(R.id.welcomeText);
            tvPendingCount = view.findViewById(R.id.tv_pending_count);
            tvApprovedCount = view.findViewById(R.id.tv_approved_count);
            cardQuickCharge = view.findViewById(R.id.cardQuickCharge);
            cardHistory = view.findViewById(R.id.cardHistory);
            cardUpcomingReservation = view.findViewById(R.id.cardUpcomingReservation);
            
            // Find the existing views in the layout
            tvUpcomingReservation = view.findViewById(R.id.reservationDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        // Quick charge action - navigate to create booking
        if (cardQuickCharge != null) {
            cardQuickCharge.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CreateBookingActivity.class);
                startActivity(intent);
            });
        }

        // History action - switch to bookings tab
        if (cardHistory != null) {
            cardHistory.setOnClickListener(v -> {
                // Switch to bookings tab in the main activity
                if (getActivity() instanceof EVOwnerMain) {
                    // This would require a method in EVOwnerMain to switch tabs
                    // For now, we'll just show a message
                }
            });
        }

        // Upcoming reservation card click
        if (cardUpcomingReservation != null) {
            cardUpcomingReservation.setOnClickListener(v -> {
                // Navigate to booking details or bookings list
                // Switch to bookings tab
            });
        }
    }

    private void loadDashboardData() {
        // Load user profile data and update welcome text
        loadUserProfile();

        // Load reservation counts
        loadReservationCounts();
        
        // Load upcoming reservation
        loadUpcomingReservation();
        
        // Load nearby stations count
        loadNearbyStationsCount();
    }

    private void loadUserProfile() {
        if (profileService == null) {
            Log.e(TAG, "ProfileService is null");
            setWelcomeTextFallback();
            return;
        }

        // First try to get cached profile data for quick display
        ProfileResponse cachedProfile = profileService.getCachedProfile();
        if (cachedProfile != null) {
            updateWelcomeText(cachedProfile.getDisplayName());
        } else {
            // Use fallback from preferences while loading
            String displayName = profileService.getUserDisplayName();
            updateWelcomeText(displayName);
        }

        // Get the current user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "No user ID found in preferences");
            setWelcomeTextFallback();
            return;
        }

        // Fetch fresh profile data from API
        profileService.getUserProfile(userId, new ProfileService.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse profile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        updateWelcomeText(profile.getDisplayName());
                        Log.d(TAG, "Profile loaded successfully: " + profile.getDisplayName());
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load profile: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Keep the cached/fallback name, don't show error to user
                        // Just log it for debugging
                        Log.d(TAG, "Using cached/fallback profile data due to error");
                    });
                }
            }
        });
    }

    private void updateWelcomeText(String displayName) {
        if (tvWelcomeText != null && displayName != null && !displayName.isEmpty()) {
            tvWelcomeText.setText("Welcome, " + displayName);
        } else {
            setWelcomeTextFallback();
        }
    }

    private void setWelcomeTextFallback() {
        if (tvWelcomeText != null) {
            tvWelcomeText.setText("Welcome!");
        }
    }

    private void loadReservationCounts() {
        // Mock data for pending and approved reservations
        // In a real app, these would come from the database
        
        int pendingCount = 2;
        int approvedCount = 3;
        
        // Update the count displays
        if (tvPendingCount != null) {
            tvPendingCount.setText(String.valueOf(pendingCount));
        }
        
        if (tvApprovedCount != null) {
            tvApprovedCount.setText(String.valueOf(approvedCount));
        }
    }

    private void loadUpcomingReservation() {
        // Mock data for the next upcoming reservation
        // This updates the reservation card shown in the layout
        
        // The existing layout already shows static data
        // In a real app, this would be replaced with dynamic data
    }

    private void loadNearbyStationsCount() {
        if (locationService == null || chargingStationService == null) {
            Log.e(TAG, "Services not initialized");
            return;
        }

        // Try to get location and nearby stations
        if (locationService.hasLocationPermissions()) {
            locationService.getCurrentLocation(new LocationService.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude) {
                    // Get nearby stations within 5km radius
                    chargingStationService.getNearbyStations(latitude, longitude, 5.0,
                        new ChargingStationService.ChargingStationsCallback() {
                            @Override
                            public void onSuccess(List<ChargingStation> stations) {
                                if (getActivity() != null && isAdded()) {
                                    getActivity().runOnUiThread(() -> {
                                        updateNearbyStationsCount(stations.size());
                                    });
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "Failed to load nearby stations: " + errorMessage);
                                // Fallback to total count
                                loadTotalStationsCount();
                            }
                        });
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Log.e(TAG, "Location error: " + errorMessage);
                    // Fallback to total count
                    loadTotalStationsCount();
                }

                @Override
                public void onPermissionRequired() {
                    Log.d(TAG, "Location permission required, using total count");
                    // Fallback to total count
                    loadTotalStationsCount();
                }
            });
        } else {
            // No location permission, get total stations count
            loadTotalStationsCount();
        }
    }

    private void loadTotalStationsCount() {
        chargingStationService.getAllChargingStations(new ChargingStationService.ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        updateNearbyStationsCount(stations.size());
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load stations: " + errorMessage);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        updateNearbyStationsCount(0);
                    });
                }
            }
        });
    }

    private void updateNearbyStationsCount(int stationCount) {
        // Update the text overlay on the nearby stations map image
        // Note: The layout currently shows this as a static overlay on the image
        // You may want to find the TextView in the layout that shows this count
        // and update it dynamically
        
        Log.d(TAG, "Found " + stationCount + " nearby stations");
        
        // If you have a TextView for showing the count, update it here:
        // if (tvNearbyStationsCount != null) {
        //     tvNearbyStationsCount.setText(stationCount + " stations nearby");
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}
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
import com.ead.zap.models.Booking;
import com.ead.zap.models.BookingStatus;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.services.BookingService;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.LocationService;
import com.ead.zap.services.ProfileService;
import com.ead.zap.ui.owner.modals.CreateBookingActivity;
import com.ead.zap.utils.PreferenceManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OwnerHomeFragment extends Fragment {
    private static final String TAG = "OwnerHomeFragment";

    private TextView tvWelcomeText, tvPendingCount, tvApprovedCount, tvNearbyStationsCount;
    private MaterialCardView cardQuickCharge, cardHistory, cardUpcomingReservation;
    private TextView tvUpcomingStatus, tvUpcomingStationName, tvUpcomingAddress, tvUpcomingTime;
    
    // Services
    private ProfileService profileService;
    private PreferenceManager preferenceManager;
    private ChargingStationService chargingStationService;
    private LocationService locationService;
    private BookingService bookingService;

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
            bookingService = new BookingService(getContext());
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
            
            // Find TextViews for upcoming reservation card
            tvUpcomingStatus = view.findViewById(R.id.tv_upcoming_status);
            tvUpcomingStationName = view.findViewById(R.id.tv_upcoming_station_name);
            tvUpcomingAddress = view.findViewById(R.id.tv_upcoming_address);
            tvUpcomingTime = view.findViewById(R.id.tv_upcoming_time);
            
            // Find TextView for nearby stations count
            tvNearbyStationsCount = view.findViewById(R.id.tv_nearby_stations_count);
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
                // Navigate to bookings tab
                if (getActivity() instanceof EVOwnerMain) {
                    Intent intent = new Intent(getActivity(), EVOwnerMain.class);
                    intent.putExtra("open_bookings_tab", true);
                    startActivity(intent);
                }
            });
        }

        // Upcoming reservation card click
        if (cardUpcomingReservation != null) {
            cardUpcomingReservation.setOnClickListener(v -> {
                // Navigate to bookings tab to show all bookings
                if (getActivity() instanceof EVOwnerMain) {
                    EVOwnerMain mainActivity = (EVOwnerMain) getActivity();
                    // The bottom navigation will handle the fragment switching
                    // We can trigger this by sending an intent or using a public method
                    Intent intent = new Intent(getActivity(), EVOwnerMain.class);
                    intent.putExtra("open_bookings_tab", true);
                    startActivity(intent);
                }
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
        if (bookingService == null) {
            Log.e(TAG, "BookingService is null");
            return;
        }

        // Set loading state
        if (tvPendingCount != null) {
            tvPendingCount.setText("...");
        }
        if (tvApprovedCount != null) {
            tvApprovedCount.setText("...");
        }

        // Get all bookings to calculate counts
        bookingService.getAllBookings(new BookingService.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Calculate counts by status
                        int pendingCount = 0;
                        int approvedCount = 0;
                        int inProgressCount = 0;
                        int completedCount = 0;
                        int cancelledCount = 0;
                        
                        Log.d(TAG, "Processing " + bookings.size() + " bookings for status counts");
                        
                        for (Booking booking : bookings) {
                            BookingStatus status = booking.getStatus();
                            Log.d(TAG, "Booking ID: " + booking.getBookingId() + 
                                      ", Status: " + status + 
                                      " (" + booking.getStatusString() + ")");
                            
                            switch (status) {
                                case PENDING:
                                    pendingCount++;
                                    break;
                                case APPROVED:
                                    approvedCount++;
                                    break;
                                case IN_PROGRESS:
                                    inProgressCount++;
                                    break;
                                case COMPLETED:
                                    completedCount++;
                                    break;
                                case CANCELLED:
                                case NO_SHOW:
                                    cancelledCount++;
                                    break;
                            }
                        }
                        
                        Log.d(TAG, "Status counts - Pending: " + pendingCount + 
                                   ", Approved: " + approvedCount + 
                                   ", In Progress: " + inProgressCount + 
                                   ", Completed: " + completedCount + 
                                   ", Cancelled/No-show: " + cancelledCount);
                        
                        // Update the count displays
                        if (tvPendingCount != null) {
                            tvPendingCount.setText(String.valueOf(pendingCount));
                        }
                        
                        if (tvApprovedCount != null) {
                            tvApprovedCount.setText(String.valueOf(approvedCount));
                        }
                        
                        Log.d(TAG, "Loaded booking counts - Pending: " + pendingCount + ", Approved: " + approvedCount);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load booking counts: " + errorMessage);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Set counts to 0 on error
                        if (tvPendingCount != null) {
                            tvPendingCount.setText("0");
                        }
                        if (tvApprovedCount != null) {
                            tvApprovedCount.setText("0");
                        }
                    });
                }
            }
        });
    }

    private void loadUpcomingReservation() {
        if (bookingService == null) {
            Log.e(TAG, "BookingService is null");
            hideUpcomingReservationCard();
            return;
        }

        // Show loading state
        if (tvUpcomingStationName != null) {
            tvUpcomingStationName.setText("Loading...");
        }

        // Get upcoming bookings to find the next one
        bookingService.getUpcomingBookings(new BookingService.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (bookings != null && !bookings.isEmpty()) {
                            // Show the first upcoming booking
                            Booking nextBooking = bookings.get(0);
                            displayUpcomingReservation(nextBooking);
                        } else {
                            // No upcoming reservations
                            hideUpcomingReservationCard();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load upcoming reservations: " + errorMessage);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        hideUpcomingReservationCard();
                    });
                }
            }
        });
    }

    private void displayUpcomingReservation(Booking booking) {
        if (tvUpcomingStatus != null) {
            tvUpcomingStatus.setText(booking.getStatusString());
            
            // Set status color based on booking status
            int colorRes;
            switch (booking.getStatus()) {
                case PENDING:
                    colorRes = R.color.amber_600;
                    break;
                case APPROVED:
                    colorRes = R.color.primary_light;
                    break;
                case IN_PROGRESS:
                    colorRes = R.color.primary_dark;
                    break;
                default:
                    colorRes = R.color.gray_600;
            }
            tvUpcomingStatus.setTextColor(getResources().getColor(colorRes, null));
        }
        
        if (tvUpcomingStationName != null) {
            tvUpcomingStationName.setText(booking.getStationName() != null ? booking.getStationName() : "Station");
        }
        
        if (tvUpcomingAddress != null) {
            tvUpcomingAddress.setText(booking.getStationAddress() != null ? booking.getStationAddress() : "Address not available");
        }
        
        if (tvUpcomingTime != null) {
            String timeText = booking.getDate() + ", " + booking.getTime();
            if (booking.getDuration() > 0) {
                timeText += " (" + booking.getDuration() + " min)";
            }
            tvUpcomingTime.setText(timeText);
        }
        
        // Show the reservation card
        if (cardUpcomingReservation != null) {
            cardUpcomingReservation.setVisibility(View.VISIBLE);
        }
        
        Log.d(TAG, "Displayed upcoming reservation: " + booking.getStationName());
    }

    private void hideUpcomingReservationCard() {
        if (cardUpcomingReservation != null) {
            cardUpcomingReservation.setVisibility(View.GONE);
        }
        Log.d(TAG, "No upcoming reservations to display");
    }

    private void loadNearbyStationsCount() {
        if (locationService == null || chargingStationService == null) {
            Log.e(TAG, "Services not initialized");
            return;
        }

        // Show loading state
        if (tvNearbyStationsCount != null) {
            tvNearbyStationsCount.setText("Loading...");
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
        Log.d(TAG, "Found " + stationCount + " nearby stations");
        
        if (tvNearbyStationsCount != null) {
            String countText = stationCount + " stations within 5 km";
            tvNearbyStationsCount.setText(countText);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}
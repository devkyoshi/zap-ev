package com.ead.zap.ui.owner.modals;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.adapters.StationSelectionAdapter;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.LocationService;

import java.util.ArrayList;
import java.util.List;

public class StationSelectionActivity extends AppCompatActivity {
    private static final String TAG = "StationSelectionActivity";

    private RecyclerView recyclerView;
    private StationSelectionAdapter adapter;
    private ChargingStationService chargingStationService;
    private LocationService locationService;
    private View progressBar, emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "StationSelectionActivity onCreate called");
        setContentView(R.layout.activity_station_selection);

        initViews();
        initServices();
        setupRecyclerView();
        loadStations();

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Charging Station");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewStations);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        
        // Setup retry button
        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            Log.d(TAG, "Retry button clicked");
            loadStations();
        });
    }

    private void initServices() {
        chargingStationService = new ChargingStationService(this);
        locationService = new LocationService(this);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        adapter = new StationSelectionAdapter(this, new ArrayList<>(), this::onStationSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete - adapter: " + (adapter != null ? "created" : "null"));
    }

    private void loadStations() {
        showLoading(true);

        // Try to get location and nearby stations first
        if (locationService.hasLocationPermissions()) {
            locationService.getCurrentLocation(new LocationService.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude) {
                    // Get nearby stations within 10km radius
                    chargingStationService.getNearbyStations(latitude, longitude, 10.0,
                        new ChargingStationService.ChargingStationsCallback() {
                            @Override
                            public void onSuccess(List<ChargingStation> stations) {
                                runOnUiThread(() -> {
                                    showLoading(false);
                                    updateStationList(stations);
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "Failed to load nearby stations: " + errorMessage);
                                runOnUiThread(() -> {
                                    Toast.makeText(StationSelectionActivity.this, 
                                        "Failed to load nearby stations: " + errorMessage, 
                                        Toast.LENGTH_SHORT).show();
                                });
                                // Fallback to all stations
                                loadAllStations();
                            }
                        });
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Log.e(TAG, "Location error: " + errorMessage);
                    // Fallback to all stations
                    loadAllStations();
                }

                @Override
                public void onPermissionRequired() {
                    Log.d(TAG, "Location permission required, loading all stations");
                    // Fallback to all stations
                    loadAllStations();
                }
            });
        } else {
            // No location permission, load all stations
            loadAllStations();
        }
    }

    private void loadAllStations() {
        Log.d(TAG, "Loading all charging stations...");
        // Use getAllChargingStations instead of getAvailableChargingStations for debugging
        chargingStationService.getAllChargingStations(new ChargingStationService.ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                Log.d(TAG, "Successfully loaded " + stations.size() + " stations");
                for (int i = 0; i < Math.min(stations.size(), 3); i++) {
                    ChargingStation station = stations.get(i);
                    Log.d(TAG, "Station " + i + ": " + station.getName() + 
                          " - Available: " + station.getAvailableSlots() + 
                          " - Active: " + station.isActive());
                }
                runOnUiThread(() -> {
                    showLoading(false);
                    updateStationList(stations);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load stations: " + errorMessage);
                runOnUiThread(() -> {
                    showLoading(false);
                    // If it's an authentication error, suggest login
                    if (errorMessage.toLowerCase().contains("not authenticated") || 
                        errorMessage.toLowerCase().contains("unauthorized") ||
                        errorMessage.toLowerCase().contains("401")) {
                        showError("Please log in again to view charging stations");
                        Toast.makeText(StationSelectionActivity.this, 
                            "Authentication error - please log in again", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        showError("Failed to load stations: " + errorMessage);
                        Toast.makeText(StationSelectionActivity.this, 
                            "Error: " + errorMessage, 
                            Toast.LENGTH_LONG).show();
                        
                        // For debugging: try to load mock data
                        Log.d(TAG, "Attempting to load mock data for testing...");
                        loadMockStationsForTesting();
                    }
                });
            }
        });
    }

    private void updateStationList(List<ChargingStation> stations) {
        Log.d(TAG, "updateStationList called with " + stations.size() + " stations");
        if (stations.isEmpty()) {
            Log.d(TAG, "No stations to display, showing empty view");
            showEmptyView(true);
        } else {
            Log.d(TAG, "Displaying " + stations.size() + " stations in RecyclerView");
            showEmptyView(false);
            if (adapter != null) {
                adapter.updateStations(stations);
                Log.d(TAG, "Adapter updated successfully");
            } else {
                Log.e(TAG, "Adapter is null!");
            }
        }
    }

    private void onStationSelected(ChargingStation station) {
        Log.d(TAG, "Station selected: " + station.getName() + " (ID: " + station.getId() + ")");
        
        // Return selected station to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_station_id", station.getId());
        resultIntent.putExtra("selected_station_name", station.getName());
        resultIntent.putExtra("selected_station_address", 
            station.getLocation() != null ? station.getLocation().getAddress() : "No address");
        resultIntent.putExtra("selected_station_price", station.getPricePerHour());
        resultIntent.putExtra("selected_station_available_slots", station.getAvailableSlots());
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + show);
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            Log.e(TAG, "progressBar is null!");
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        } else {
            Log.e(TAG, "recyclerView is null!");
        }
    }

    private void showEmptyView(boolean show) {
        Log.d(TAG, "showEmptyView: " + show);
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            Log.e(TAG, "emptyView is null!");
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        } else {
            Log.e(TAG, "recyclerView is null!");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyView(true);
    }

    /**
     * Load mock stations for testing when API fails
     */
    private void loadMockStationsForTesting() {
        Log.d(TAG, "Loading mock stations for testing...");
        
        List<ChargingStation> mockStations = new ArrayList<>();
        
        // Create mock station 1
        ChargingStation station1 = new ChargingStation();
        station1.setId("mock-001");
        station1.setName("Test Station 1");
        station1.setPricePerHour(150.0);
        station1.setTotalSlots(10);
        station1.setAvailableSlots(7);
        station1.setActive(true);
        
        ChargingStation.Location location1 = new ChargingStation.Location();
        location1.setLatitude(6.9271);
        location1.setLongitude(79.8612);
        location1.setAddress("123 Test Street, Colombo");
        location1.setCity("Colombo");
        station1.setLocation(location1);
        
        mockStations.add(station1);
        
        // Create mock station 2
        ChargingStation station2 = new ChargingStation();
        station2.setId("mock-002");
        station2.setName("Test Station 2");
        station2.setPricePerHour(200.0);
        station2.setTotalSlots(8);
        station2.setAvailableSlots(5);
        station2.setActive(true);
        
        ChargingStation.Location location2 = new ChargingStation.Location();
        location2.setLatitude(6.9319);
        location2.setLongitude(79.8478);
        location2.setAddress("456 Mock Avenue, Colombo");
        location2.setCity("Colombo");
        station2.setLocation(location2);
        
        mockStations.add(station2);
        
        Log.d(TAG, "Created " + mockStations.size() + " mock stations");
        updateStationList(mockStations);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
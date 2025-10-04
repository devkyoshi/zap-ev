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
    }

    private void initServices() {
        chargingStationService = new ChargingStationService(this);
        locationService = new LocationService(this);
    }

    private void setupRecyclerView() {
        adapter = new StationSelectionAdapter(this, new ArrayList<>(), this::onStationSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
        chargingStationService.getAvailableChargingStations(new ChargingStationService.ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateStationList(stations);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to load stations: " + errorMessage);
                });
            }
        });
    }

    private void updateStationList(List<ChargingStation> stations) {
        if (stations.isEmpty()) {
            showEmptyView(true);
        } else {
            showEmptyView(false);
            adapter.updateStations(stations);
        }
    }

    private void onStationSelected(ChargingStation station) {
        // Return selected station to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_station_id", station.getId());
        resultIntent.putExtra("selected_station_name", station.getName());
        resultIntent.putExtra("selected_station_address", station.getLocation().getAddress());
        resultIntent.putExtra("selected_station_price", station.getPricePerHour());
        resultIntent.putExtra("selected_station_available_slots", station.getAvailableSlots());
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyView(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
package com.ead.zap.ui.owner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.LocationService;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class OwnerMapsFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private LocationService locationService;
    private ChargingStationService chargingStationService;
    private double currentLatitude = 6.9271; // Default to Colombo, Sri Lanka
    private double currentLongitude = 79.8612;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize osmdroid configuration
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        
        locationService = new LocationService(requireContext());
        chargingStationService = new ChargingStationService(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_maps, container, false);
        
        mapView = view.findViewById(R.id.mapView);
        setupMap();
        setupButtons(view);
        
        return view;
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (locationService.hasLocationPermissions()) {
                getCurrentLocationAndLoadStations();
            } else {
                requestLocationPermissions();
            }
        });

        view.findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            loadNearbyStations();
        });
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);

        // Check for location permissions and get current location
        checkLocationPermissionAndLoadData();
    }

    private void checkLocationPermissionAndLoadData() {
        if (locationService.hasLocationPermissions()) {
            getCurrentLocationAndLoadStations();
        } else {
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        String[] permissions = LocationService.getRequiredPermissions();
        ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getCurrentLocationAndLoadStations() {
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                
                // Update map center
                GeoPoint userLocation = new GeoPoint(latitude, longitude);
                mapView.getController().setCenter(userLocation);
                
                // Add user location marker
                addUserLocationMarker(latitude, longitude);
                
                // Load nearby stations
                loadNearbyStations();
            }

            @Override
            public void onLocationError(String errorMessage) {
                Toast.makeText(requireContext(), "Location error: " + errorMessage, Toast.LENGTH_SHORT).show();
                
                // Use default location (Colombo) and load all stations
                GeoPoint defaultLocation = new GeoPoint(currentLatitude, currentLongitude);
                mapView.getController().setCenter(defaultLocation);
                loadAllStations();
            }

            @Override
            public void onPermissionRequired() {
                requestLocationPermissions();
            }
        });
    }

    private void addUserLocationMarker(double latitude, double longitude) {
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(new GeoPoint(latitude, longitude));
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setTitle("Your Location");
        
        // Set user location icon (you can customize this)
        try {
            Drawable userIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location);
            if (userIcon != null) {
                userMarker.setIcon(userIcon);
            }
        } catch (Exception e) {
            // Use default marker if custom icon fails
        }
        
        mapView.getOverlays().add(userMarker);
        mapView.invalidate();
    }

    private void loadNearbyStations() {
        chargingStationService.getNearbyStations(currentLatitude, currentLongitude, 
            new ChargingStationService.ChargingStationsCallback() {
                @Override
                public void onSuccess(List<ChargingStation> stations) {
                    addStationMarkersToMap(stations);
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(requireContext(), "Failed to load nearby stations: " + errorMessage, Toast.LENGTH_SHORT).show();
                    // Fallback to loading all stations
                    loadAllStations();
                }
            });
    }

    private void loadAllStations() {
        chargingStationService.getAllChargingStations(new ChargingStationService.ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                addStationMarkersToMap(stations);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to load stations: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStationMarkersToMap(List<ChargingStation> stations) {
        // Clear existing station markers (keep user location marker)
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker && 
            !((Marker) overlay).getTitle().equals("Your Location"));

        for (ChargingStation station : stations) {
            if (station.getLocation() != null) {
                addStationMarker(station);
            }
        }
        
        mapView.invalidate();
    }

    private void addStationMarker(ChargingStation station) {
        Marker stationMarker = new Marker(mapView);
        GeoPoint stationPoint = new GeoPoint(
            station.getLocation().getLatitude(), 
            station.getLocation().getLongitude()
        );
        stationMarker.setPosition(stationPoint);
        stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        
        // Set marker title and description
        stationMarker.setTitle(station.getName());
        String description = String.format("%s\n%s\n%s", 
            station.getLocation().getAddress(),
            station.getAvailabilityText(),
            station.getFormattedPrice()
        );
        stationMarker.setSubDescription(description);
        
        // Set station marker icon
        try {
            Drawable stationIcon;
            if (station.getAvailableSlots() > 0) {
                stationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_charging);
            } else {
                stationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_charging);
                // You can use a different icon for unavailable stations
            }
            if (stationIcon != null) {
                stationMarker.setIcon(stationIcon);
            }
        } catch (Exception e) {
            // Use default marker if custom icon fails
        }
        
        // Add click listener for station marker
        stationMarker.setOnMarkerClickListener((marker, mapView) -> {
            // Show station details or navigate to booking
            showStationDetails(station);
            return true;
        });
        
        mapView.getOverlays().add(stationMarker);
    }

    private void showStationDetails(ChargingStation station) {
        String details = String.format(
            "Station: %s\nAddress: %s\nAvailable Slots: %d/%d\nPrice: %s\nType: %s",
            station.getName(),
            station.getLocation().getAddress(),
            station.getAvailableSlots(),
            station.getTotalSlots(),
            station.getFormattedPrice(),
            station.getType()
        );
        
        Toast.makeText(requireContext(), details, Toast.LENGTH_LONG).show();
        
        // TODO: You can implement a proper dialog or navigate to booking activity
        // Intent intent = new Intent(requireContext(), CreateBookingActivity.class);
        // intent.putExtra("station_id", station.getId());
        // startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean locationGranted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    locationGranted = true;
                    break;
                }
            }
            
            if (locationGranted) {
                getCurrentLocationAndLoadStations();
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show();
                // Use default location
                GeoPoint defaultLocation = new GeoPoint(currentLatitude, currentLongitude);
                mapView.getController().setCenter(defaultLocation);
                loadAllStations();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationService != null) {
            locationService.stopLocationUpdates();
        }
    }
}

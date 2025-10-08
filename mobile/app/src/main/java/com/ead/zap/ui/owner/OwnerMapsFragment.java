package com.ead.zap.ui.owner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.adapters.StationMapListAdapter;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.LocationService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;
import java.util.List;

public class OwnerMapsFragment extends Fragment implements StationMapListAdapter.OnStationClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private LocationService locationService;
    private ChargingStationService chargingStationService;
    private double currentLatitude = 6.9271; // Default to Colombo, Sri Lanka
    private double currentLongitude = 79.8612;
    
    // UI Components
    private TextView tvStationCount;
    private RecyclerView recyclerViewStations;
    private View loadingView, emptyView;
    private FloatingActionButton fabMyLocation, fabRefresh;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private StationMapListAdapter stationAdapter;
    
    // Data
    private List<ChargingStation> allStations = new ArrayList<>();
    private Marker selectedStationMarker;
    private Polyline routeOverlay;
    private RoadManager roadManager;

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
        
        initViews(view);
        setupMap();
        setupBottomSheet(view);
        setupButtons();
        
        return view;
    }
    
    private void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        tvStationCount = view.findViewById(R.id.tvStationCount);
        recyclerViewStations = view.findViewById(R.id.recyclerViewStations);
        loadingView = view.findViewById(R.id.loadingView);
        emptyView = view.findViewById(R.id.emptyView);
        fabMyLocation = view.findViewById(R.id.fabMyLocation);
        fabRefresh = view.findViewById(R.id.fabRefresh);
    }
    
    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        
        // Configure bottom sheet behavior
        bottomSheetBehavior.setPeekHeight(140);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);
        
        // Set initial state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        // Setup RecyclerView
        stationAdapter = new StationMapListAdapter(requireContext(), allStations, this);
        recyclerViewStations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewStations.setAdapter(stationAdapter);
        
        // Setup show list button
        view.findViewById(R.id.btnShowList).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        
        // Add callback to update button text
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                com.google.android.material.button.MaterialButton btnShowList = 
                    view.findViewById(R.id.btnShowList);
                
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        btnShowList.setText("View List");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        btnShowList.setText("Expand");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        btnShowList.setText("Collapse");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Optional: Add slide animations here
            }
        });
    }

    private void setupButtons() {
        fabMyLocation.setOnClickListener(v -> {
            if (locationService.hasLocationPermissions()) {
                getCurrentLocationAndLoadStations();
            } else {
                requestLocationPermissions();
            }
        });

        fabRefresh.setOnClickListener(v -> {
            clearRoute(); // Clear any existing route
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
        
        // Also try to load all stations immediately for testing
        loadAllStations();
        
        // For testing: Add some mock stations if API fails
        addMockStationsForTesting();
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
        android.util.Log.d("OwnerMapsFragment", "Loading nearby stations at: " + currentLatitude + ", " + currentLongitude);
        showLoadingState();
        
        chargingStationService.getNearbyStations(currentLatitude, currentLongitude, 
            new ChargingStationService.ChargingStationsCallback() {
                @Override
                public void onSuccess(List<ChargingStation> stations) {
                    android.util.Log.d("OwnerMapsFragment", "Successfully loaded " + stations.size() + " nearby stations");
                    
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            addStationMarkersToMap(stations);
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("OwnerMapsFragment", "Failed to load nearby stations: " + errorMessage);
                    
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to load nearby stations: " + errorMessage, Toast.LENGTH_SHORT).show();
                            // Fallback to loading all stations
                            loadAllStations();
                        });
                    }
                }
            });
    }

    private void loadAllStations() {
        android.util.Log.d("OwnerMapsFragment", "Loading all stations...");
        showLoadingState();
        
        chargingStationService.getAllChargingStations(new ChargingStationService.ChargingStationsCallback() {
            @Override
            public void onSuccess(List<ChargingStation> stations) {
                android.util.Log.d("OwnerMapsFragment", "Successfully loaded " + stations.size() + " stations");
                
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        addStationMarkersToMap(stations);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("OwnerMapsFragment", "Failed to load stations: " + errorMessage);
                
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load stations: " + errorMessage, Toast.LENGTH_SHORT).show();
                        showEmptyState();
                        updateStationCount(0);
                    });
                }
            }
        });
    }
    
    // Implement StationMapListAdapter.OnStationClickListener
    @Override
    public void onStationClick(ChargingStation station) {
        showStationDetails(station);
    }
    
    @Override
    public void onNavigateClick(ChargingStation station) {
        if (station.getLocation() != null) {
            // Use the proper navigation system with street-level routing
            navigateToStation(station);
            
            // Show navigation info
            String message = String.format("Starting navigation to %s", station.getName());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            
            // Collapse bottom sheet to focus on navigation
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            Toast.makeText(requireContext(), "Station location not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void drawRouteToStation(ChargingStation station) {
        // Remove existing route lines
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Polyline);
        
        // Create a simple straight line route (for basic navigation)
        Polyline routeLine = new Polyline();
        List<GeoPoint> routePoints = new ArrayList<>();
        
        // Add current location
        routePoints.add(new GeoPoint(currentLatitude, currentLongitude));
        
        // Add station location
        routePoints.add(new GeoPoint(
            station.getLocation().getLatitude(),
            station.getLocation().getLongitude()
        ));
        
        routeLine.setPoints(routePoints);
        routeLine.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
        routeLine.setWidth(8.0f);
        
        mapView.getOverlays().add(routeLine);
        mapView.invalidate();
        
        // Adjust map view to show the route
        if (routePoints.size() >= 2) {
            // Calculate bounds and adjust map
            double minLat = Math.min(currentLatitude, station.getLocation().getLatitude());
            double maxLat = Math.max(currentLatitude, station.getLocation().getLatitude());
            double minLon = Math.min(currentLongitude, station.getLocation().getLongitude());
            double maxLon = Math.max(currentLongitude, station.getLocation().getLongitude());
            
            // Add some padding
            double latPadding = (maxLat - minLat) * 0.2;
            double lonPadding = (maxLon - minLon) * 0.2;
            
            GeoPoint center = new GeoPoint(
                (minLat + maxLat) / 2, 
                (minLon + maxLon) / 2
            );
            
            mapView.getController().animateTo(center);
            
            // Set appropriate zoom level
            double distance = LocationService.calculateDistance(
                currentLatitude, currentLongitude,
                station.getLocation().getLatitude(),
                station.getLocation().getLongitude()
            );
            
            if (distance < 1) {
                mapView.getController().setZoom(16.0);
            } else if (distance < 5) {
                mapView.getController().setZoom(14.0);
            } else {
                mapView.getController().setZoom(12.0);
            }
        }
    }

    private void addStationMarkersToMap(List<ChargingStation> stations) {
        // Clear existing station markers (keep user location marker and routes)
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker && 
            !((Marker) overlay).getTitle().equals("Your Location"));

        // Update stations list
        allStations.clear();
        allStations.addAll(stations);
        
        // Calculate distances from current location if available
        for (ChargingStation station : stations) {
            if (station.getLocation() != null) {
                // Calculate distance if we have current location
                if (currentLatitude != 6.9271 || currentLongitude != 79.8612) {
                    double distance = LocationService.calculateDistance(
                        currentLatitude, currentLongitude,
                        station.getLocation().getLatitude(),
                        station.getLocation().getLongitude()
                    );
                    station.setDistance(distance);
                }
                addStationMarker(station);
            }
        }
        
        // Update UI
        updateStationCount(stations.size());
        updateStationList();
        
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
        // Focus on the station marker
        if (station.getLocation() != null) {
            GeoPoint stationPoint = new GeoPoint(
                station.getLocation().getLatitude(), 
                station.getLocation().getLongitude()
            );
            mapView.getController().animateTo(stationPoint);
            mapView.getController().setZoom(16.0);
        }
        
        // Expand bottom sheet to show details
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        
        // Highlight the station in the list (scroll to it)
        int position = allStations.indexOf(station);
        if (position >= 0) {
            recyclerViewStations.scrollToPosition(position);
        }
    }
    
    private void updateStationCount(int count) {
        if (tvStationCount != null) {
            if (count == 0) {
                tvStationCount.setText("No stations found");
            } else if (count == 1) {
                tvStationCount.setText("1 station found");
            } else {
                tvStationCount.setText(count + " stations found");
            }
            
            // Debug log
            android.util.Log.d("OwnerMapsFragment", "Updated station count: " + count);
        }
    }
    
    private void updateStationList() {
        android.util.Log.d("OwnerMapsFragment", "Updating station list. Stations count: " + allStations.size());
        android.util.Log.d("OwnerMapsFragment", "RecyclerView adapter: " + (stationAdapter != null ? "exists" : "null"));
        android.util.Log.d("OwnerMapsFragment", "RecyclerView: " + (recyclerViewStations != null ? "exists" : "null"));
        
        if (allStations.isEmpty()) {
            android.util.Log.d("OwnerMapsFragment", "Showing empty state");
            showEmptyState();
        } else {
            android.util.Log.d("OwnerMapsFragment", "Showing station list");
            showStationList();
            
            // Update the adapter with new data
            if (stationAdapter != null) {
                android.util.Log.d("OwnerMapsFragment", "Updating adapter with new station list");
                // Create a copy to avoid reference issues
                List<ChargingStation> stationsCopy = new ArrayList<>(allStations);
                stationAdapter.updateStations(stationsCopy);
            }
            
            // Debug log
            for (int i = 0; i < allStations.size(); i++) {
                ChargingStation station = allStations.get(i);
                String locationText = (station.getLocation() != null && station.getLocation().getAddress() != null) 
                    ? station.getLocation().getAddress() 
                    : "No address";
                android.util.Log.d("OwnerMapsFragment", "Station " + i + ": " + station.getName() + " - " + locationText);
            }
        }
    }
    
    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerViewStations.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void showStationList() {
        android.util.Log.d("OwnerMapsFragment", "showStationList() called");
        loadingView.setVisibility(View.GONE);
        recyclerViewStations.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        android.util.Log.d("OwnerMapsFragment", "RecyclerView visibility set to VISIBLE");
    }
    
    private void showEmptyState() {
        loadingView.setVisibility(View.GONE);
        recyclerViewStations.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
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
    
    // For testing purposes - add mock stations if API is not working
    private void addMockStationsForTesting() {
        android.util.Log.d("OwnerMapsFragment", "Adding mock stations for testing");
        
        List<ChargingStation> mockStations = new ArrayList<>();
        
        // Create mock station 1 - Near Colombo
        ChargingStation station1 = new ChargingStation();
        station1.setId("mock_1");
        station1.setName("EcoCharge Hub - Colombo");
        station1.setType("Fast Charging");
        station1.setTotalSlots(8);
        station1.setAvailableSlots(3);
        station1.setPricePerHour(25.50);
        station1.setActive(true);
        station1.setDistance(2.5); // 2.5 km away
        
        ChargingStation.Location location1 = new ChargingStation.Location();
        location1.setLatitude(6.9271);
        location1.setLongitude(79.8612);
        location1.setAddress("123 Galle Road, Colombo 03");
        location1.setCity("Colombo");
        station1.setLocation(location1);
        
        // Create mock station 2 - Slightly north
        ChargingStation station2 = new ChargingStation();
        station2.setId("mock_2");
        station2.setName("PowerUp Station - Mount Lavinia");
        station2.setType("Standard Charging");
        station2.setTotalSlots(6);
        station2.setAvailableSlots(0);
        station2.setPricePerHour(18.75);
        station2.setActive(true);
        station2.setDistance(4.1); // 4.1 km away
        
        ChargingStation.Location location2 = new ChargingStation.Location();
        location2.setLatitude(6.8389);
        location2.setLongitude(79.8617);
        location2.setAddress("456 Galle Road, Mount Lavinia");
        location2.setCity("Mount Lavinia");
        station2.setLocation(location2);
        
        // Create mock station 3 - East of Colombo
        ChargingStation station3 = new ChargingStation();
        station3.setId("mock_3");
        station3.setName("QuickCharge Center - Rajagiriya");
        station3.setType("Ultra Fast");
        station3.setTotalSlots(12);
        station3.setAvailableSlots(7);
        station3.setPricePerHour(32.00);
        station3.setActive(true);
        station3.setDistance(1.8); // 1.8 km away
        
        ChargingStation.Location location3 = new ChargingStation.Location();
        location3.setLatitude(6.9106);
        location3.setLongitude(79.8906);
        location3.setAddress("789 Kotte Road, Rajagiriya");
        location3.setCity("Rajagiriya");
        station3.setLocation(location3);
        
        mockStations.add(station1);
        mockStations.add(station2);
        mockStations.add(station3);
        
        // Add mock stations to the map and list after a small delay to simulate API call
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            if (allStations.isEmpty()) { // Only add mock data if no real data loaded
                android.util.Log.d("OwnerMapsFragment", "Adding mock stations since no real data loaded");
                addStationMarkersToMap(mockStations);
            }
        }, 3000); // 3 second delay
    }
    
    // Navigation and routing methods
    private void navigateToStation(ChargingStation station) {
        if (station == null || station.getLocation() == null) {
            Toast.makeText(getContext(), "Station location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("OwnerMapsFragment", "Starting navigation to: " + station.getName());
        
        // Clear any existing route
        clearRoute();
        
        // Show loading toast
        Toast.makeText(getContext(), "Calculating route to " + station.getName(), Toast.LENGTH_SHORT).show();
        
        // Calculate route in background thread
        new Thread(() -> {
            try {
                // Initialize road manager if not already done
                if (roadManager == null) {
                    roadManager = new OSRMRoadManager(getContext(), "ZapEV/1.0");
                }
                
                // Create waypoints (from current location to station)
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(new GeoPoint(currentLatitude, currentLongitude));
                waypoints.add(new GeoPoint(station.getLocation().getLatitude(), station.getLocation().getLongitude()));
                
                // Get the road
                Road road = roadManager.getRoad(waypoints);
                
                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    if (road != null && road.mStatus == Road.STATUS_OK) {
                        displayRoute(road, station);
                        
                        // Focus on the route
                        mapView.getController().animateTo(new GeoPoint(station.getLocation().getLatitude(), station.getLocation().getLongitude()));
                        mapView.getController().setZoom(16.0);
                        
                        Toast.makeText(getContext(), 
                            String.format("Route: %.1f km, %.0f min", 
                                road.mLength, road.mDuration / 60), 
                            Toast.LENGTH_LONG).show();
                    } else {
                        // Fallback to direct navigation
                        showDirectRoute(station);
                        Toast.makeText(getContext(), "Using direct route to station", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e("OwnerMapsFragment", "Error calculating route", e);
                requireActivity().runOnUiThread(() -> {
                    showDirectRoute(station);
                    Toast.makeText(getContext(), "Route calculation failed, showing direct path", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void displayRoute(Road road, ChargingStation station) {
        // Create polyline for the route
        routeOverlay = RoadManager.buildRoadOverlay(road);
        routeOverlay.getOutlinePaint().setColor(0xFF0066CC); // Blue color
        routeOverlay.getOutlinePaint().setStrokeWidth(12);
        
        // Add route to map
        mapView.getOverlays().add(routeOverlay);
        
        // Highlight the destination station
        highlightSelectedStation(station);
        
        // Refresh map
        mapView.invalidate();
    }
    
    private void showDirectRoute(ChargingStation station) {
        // Show a simple direct line to the station
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(currentLatitude, currentLongitude));
        geoPoints.add(new GeoPoint(station.getLocation().getLatitude(), station.getLocation().getLongitude()));
        
        routeOverlay = new Polyline();
        routeOverlay.setPoints(geoPoints);
        routeOverlay.getOutlinePaint().setColor(0xFFFF6600); // Orange color for direct route
        routeOverlay.getOutlinePaint().setStrokeWidth(8);
        routeOverlay.getOutlinePaint().setPathEffect(new android.graphics.DashPathEffect(new float[]{20, 10}, 0));
        
        mapView.getOverlays().add(routeOverlay);
        
        // Highlight the destination station
        highlightSelectedStation(station);
        
        // Focus on the station
        mapView.getController().animateTo(new GeoPoint(station.getLocation().getLatitude(), station.getLocation().getLongitude()));
        mapView.getController().setZoom(16.0);
        
        mapView.invalidate();
    }
    
    private void highlightSelectedStation(ChargingStation station) {
        // Find and highlight the selected station marker
        for (int i = 0; i < mapView.getOverlays().size(); i++) {
            if (mapView.getOverlays().get(i) instanceof Marker) {
                Marker marker = (Marker) mapView.getOverlays().get(i);
                if (station.getId().equals(marker.getId())) {
                    selectedStationMarker = marker;
                    // Change marker appearance to show it's selected
                    try {
                        Drawable selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location);
                        if (selectedDrawable != null) {
                            selectedDrawable.setTint(0xFFFF0000); // Red for selected
                            marker.setIcon(selectedDrawable);
                        }
                    } catch (Exception e) {
                        android.util.Log.w("OwnerMapsFragment", "Could not update marker icon", e);
                    }
                    break;
                }
            }
        }
    }
    
    private void clearRoute() {
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
            routeOverlay = null;
            mapView.invalidate();
        }
        
        // Reset selected station marker
        if (selectedStationMarker != null) {
            try {
                Drawable normalDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location);
                if (normalDrawable != null) {
                    normalDrawable.setTint(0xFF4CAF50); // Green for normal
                    selectedStationMarker.setIcon(normalDrawable);
                }
            } catch (Exception e) {
                android.util.Log.w("OwnerMapsFragment", "Could not reset marker icon", e);
            }
            selectedStationMarker = null;
        }
    }
}

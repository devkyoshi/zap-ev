package com.ead.zap.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

/**
 * Service class for handling location operations
 */
public class LocationService {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long MIN_TIME = 1000; // 1 second
    private static final float MIN_DISTANCE = 10; // 10 meters

    private Context context;
    private LocationManager locationManager;
    private LocationCallback locationCallback;

    // Interface for location callbacks
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String errorMessage);
        void onPermissionRequired();
    }

    // Constructor
    public LocationService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get current location
     */
    public void getCurrentLocation(LocationCallback callback) {
        this.locationCallback = callback;

        if (!hasLocationPermissions()) {
            callback.onPermissionRequired();
            return;
        }

        if (locationManager == null) {
            callback.onLocationError("Location manager not available");
            return;
        }

        // Check if location services are enabled
        if (!isLocationEnabled()) {
            callback.onLocationError("Location services are disabled. Please enable GPS.");
            return;
        }

        try {
            // Try to get last known location first
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                callback.onLocationReceived(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                return;
            }

            // If no last known location, request updates
            requestLocationUpdates();

        } catch (SecurityException e) {
            callback.onLocationError("Location permission denied");
        } catch (Exception e) {
            callback.onLocationError("Failed to get location: " + e.getMessage());
        }
    }

    /**
     * Check if location services are enabled
     */
    public boolean isLocationEnabled() {
        if (locationManager == null) return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) 
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Get last known location
     */
    private Location getLastKnownLocation() throws SecurityException {
        Location location = null;

        // Try GPS provider first
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        // If GPS location is not available, try network provider
        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    /**
     * Request location updates
     */
    private void requestLocationUpdates() throws SecurityException {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (locationCallback != null) {
                    locationCallback.onLocationReceived(location.getLatitude(), location.getLongitude());
                }
                // Stop listening after getting first location
                stopLocationUpdates();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                if (locationCallback != null) {
                    locationCallback.onLocationError("Location provider disabled");
                }
            }
        };

        // Request updates from both providers
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }
    }

    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {}
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                });
            } catch (SecurityException e) {
                // Ignore security exception when stopping updates
            }
        }
    }

    /**
     * Calculate distance between two points in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c; // Distance in km

        return distance;
    }

    /**
     * Format distance for display
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Get required permissions for location access
     */
    public static String[] getRequiredPermissions() {
        return new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }
}
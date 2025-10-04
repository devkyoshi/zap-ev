package com.ead.zap.examples;

import android.content.Context;
import android.util.Log;

import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.models.auth.EVOwnerRegistrationRequest;
import com.ead.zap.models.EVOwner;
import com.ead.zap.services.AuthService;

import java.util.ArrayList;
import java.util.List;

/**
 * Example class showing how to use the AuthService
 * Mobile app supports: EV Owners & Station Operators only
 * BackOffice users should use the web interface
 */
public class AuthServiceExample {
    private static final String TAG = "AuthServiceExample";
    private AuthService authService;

    public AuthServiceExample(Context context) {
        this.authService = new AuthService(context);
    }

    /**
     * Example: EV Owner Login
     */
    public void loginEVOwnerExample(String nic, String password) {
        authService.loginEVOwner(nic, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d(TAG, "EV Owner login successful!");
                Log.d(TAG, "User ID: " + response.getUserId());
                Log.d(TAG, "User Type: " + response.getUserType());
                Log.d(TAG, "Access Token: " + response.getAccessToken().substring(0, 20) + "...");
                
                // Proceed to main EV Owner activity
                // Intent intent = new Intent(context, EVOwnerMain.class);
                // context.startActivity(intent);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "EV Owner login failed: " + error);
                // Show error message to user
                // Toast.makeText(context, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Example: Regular User Login (BackOffice/StationOperator)
     */
    public void loginUserExample(String username, String password) {
        authService.login(username, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d(TAG, "User login successful!");
                
                // Check user type and redirect accordingly
                if (response.isBackOffice()) {
                    Log.d(TAG, "BackOffice users should use web interface");
                    // Show message directing to web app
                } else if (response.isStationOperator()) {
                    Log.d(TAG, "Redirecting to Station Operator dashboard");
                    // Intent intent = new Intent(context, StationOperatorMain.class);
                    // context.startActivity(intent);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "User login failed: " + error);
                // Show error message to user
            }
        });
    }

    /**
     * Example: EV Owner Registration
     */
    public void registerEVOwnerExample() {
        // Create registration request
        EVOwnerRegistrationRequest request = new EVOwnerRegistrationRequest();
        request.setNic("123456789V");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+94771234567");
        request.setPassword("securePassword123");

        // Add vehicle details
        List<EVOwnerRegistrationRequest.VehicleDetail> vehicles = new ArrayList<>();
        EVOwnerRegistrationRequest.VehicleDetail vehicle = new EVOwnerRegistrationRequest.VehicleDetail();
        vehicle.setMake("Tesla");
        vehicle.setModel("Model 3");
        vehicle.setLicensePlate("CAR-1234");
        vehicle.setYear(2023);
        vehicles.add(vehicle);
        request.setVehicleDetails(vehicles);

        authService.registerEVOwner(request, new AuthService.RegistrationCallback() {
            @Override
            public void onSuccess(EVOwner evOwner) {
                Log.d(TAG, "Registration successful!");
                Log.d(TAG, "EV Owner ID: " + evOwner.getId());
                Log.d(TAG, "Full Name: " + evOwner.getFullName());
                
                // Redirect to login or auto-login
                // You might want to automatically log the user in after registration
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Registration failed: " + error);
                // Show error message to user
            }
        });
    }

    /**
     * Example: Token Refresh
     */
    public void refreshTokenExample() {
        if (authService.isTokenExpired()) {
            Log.d(TAG, "Access token expired, refreshing...");
            
            authService.refreshToken(new AuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthResponse response) {
                    Log.d(TAG, "Token refresh successful!");
                    // Continue with API calls
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Token refresh failed: " + error);
                    // Redirect to login
                    // Intent intent = new Intent(context, LoginActivity.class);
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // context.startActivity(intent);
                }
            });
        } else {
            Log.d(TAG, "Token is still valid");
        }
    }

    /**
     * Example: Logout
     */
    public void logoutExample() {
        authService.logout(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d(TAG, "Logout successful!");
                
                // Redirect to login screen
                // Intent intent = new Intent(context, LoginActivity.class);
                // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // context.startActivity(intent);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Logout error: " + error);
                // Even if logout fails, still redirect to login
            }
        });
    }

    /**
     * Example: Check Authentication Status
     */
    public void checkAuthStatusExample() {
        if (authService.isAuthenticated()) {
            Log.d(TAG, "User is authenticated");
            Log.d(TAG, "User Type: " + authService.getCurrentUserType());
            Log.d(TAG, "User ID: " + authService.getCurrentUserId());
            
            // Check if token needs refresh
            if (authService.isTokenExpired()) {
                Log.d(TAG, "Token expired, need to refresh");
                refreshTokenExample();
            } else {
                Log.d(TAG, "Token is valid, proceeding to main app");
            }
        } else {
            Log.d(TAG, "User is not authenticated, redirecting to login");
            // Redirect to login
        }
    }
}
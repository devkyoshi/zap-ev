package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.api.services.AuthApiService;
import com.ead.zap.database.dao.EVOwnerDAO;
import com.ead.zap.models.EVOwner;
import com.ead.zap.models.User;
import com.ead.zap.models.auth.*;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Authentication service to handle all authentication operations
 * Manages API calls, token handling, and local data storage
 */
public class AuthService {
    private static final String TAG = "AuthService";
    
    private final Context context;
    private final AuthApiService authApiService;
    private final PreferenceManager preferenceManager;
    private final EVOwnerDAO evOwnerDAO;

    public AuthService(Context context) {
        this.context = context.getApplicationContext();
        this.authApiService = NetworkClient.getInstance(context).createService(AuthApiService.class);
        this.preferenceManager = new PreferenceManager(context);
        this.evOwnerDAO = new EVOwnerDAO(context);
    }

    /**
     * Login interface for handling authentication responses
     */
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    /**
     * Registration interface for handling registration responses
     */
    public interface RegistrationCallback {
        void onSuccess(EVOwner evOwner);
        void onError(String error);
    }

    /**
     * Login for EV Owners
     */
    public void loginEVOwner(String nic, String password, AuthCallback callback) {
        EVOwnerLoginRequest request = new EVOwnerLoginRequest(nic, password);

        Call<ApiResponse<AuthResponse>> call = authApiService.loginEVOwner(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.hasValidData()) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save authentication data
                        saveAuthData(authResponse);
                        
                        // Save the NIC for EV Owner (needed for API calls)
                        preferenceManager.setUserNIC(nic);
                        
                        Log.d(TAG, "EV Owner login successful");
                        callback.onSuccess(authResponse);
                    } else {
                        String error = apiResponse.getErrorMessage();
                        Log.e(TAG, "Login failed: " + error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Login failed: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Login for regular users (StationOperator)
     */
    public void login(String username, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(username, password);
        
        Call<ApiResponse<AuthResponse>> call = authApiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.hasValidData()) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save authentication data
                        saveAuthData(authResponse);
                        
                        Log.d(TAG, "User login successful");
                        callback.onSuccess(authResponse);
                    } else {
                        String error = apiResponse.getErrorMessage();
                        Log.e(TAG, "Login failed: " + error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Login failed: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Register new EV Owner
     */
    public void registerEVOwner(EVOwnerRegistrationRequest registrationRequest, RegistrationCallback callback) {
        Call<ApiResponse<EVOwner>> call = authApiService.registerEVOwner(registrationRequest);
        call.enqueue(new Callback<ApiResponse<EVOwner>>() {
            @Override
            public void onResponse(Call<ApiResponse<EVOwner>> call, Response<ApiResponse<EVOwner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<EVOwner> apiResponse = response.body();
                    
                    if (apiResponse.hasValidData()) {
                        EVOwner evOwner = apiResponse.getData();
                        
                        // Save EV Owner data locally
                        evOwnerDAO.insertOrUpdateEVOwner(evOwner);
                        
                        Log.d(TAG, "EV Owner registration successful");
                        callback.onSuccess(evOwner);
                    } else {
                        String error = apiResponse.getErrorMessage();
                        Log.e(TAG, "Registration failed: " + error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Registration failed: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EVOwner>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Refresh access token
     */
    public void refreshToken(AuthCallback callback) {
        String refreshToken = preferenceManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            callback.onError("No refresh token available");
            return;
        }

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        Call<ApiResponse<AuthResponse>> call = authApiService.refreshToken(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.hasValidData()) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Update stored tokens
                        saveAuthData(authResponse);
                        
                        Log.d(TAG, "Token refresh successful");
                        callback.onSuccess(authResponse);
                    } else {
                        String error = apiResponse.getErrorMessage();
                        Log.e(TAG, "Token refresh failed: " + error);
                        
                        // Clear invalid tokens
                        logout(null);
                        callback.onError(error);
                    }
                } else {
                    String error = "Token refresh failed: " + response.message();
                    Log.e(TAG, error);
                    
                    // Clear invalid tokens
                    logout(null);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Logout user
     */
    public void logout(AuthCallback callback) {
        String refreshToken = preferenceManager.getRefreshToken();
        String accessToken = preferenceManager.getAccessToken();

        if (refreshToken != null && !refreshToken.isEmpty() && 
            accessToken != null && !accessToken.isEmpty()) {
            
            LogoutRequest request = new LogoutRequest(refreshToken);
            String authHeader = "Bearer " + accessToken;
            
            Call<ApiResponse<Void>> call = authApiService.logout(authHeader, request);
            call.enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    // Clear local data regardless of API response
                    clearLocalData();
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "Logout successful");
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    } else {
                        Log.w(TAG, "Logout API call failed, but local data cleared");
                        if (callback != null) {
                            callback.onSuccess(null); // Still consider it successful locally
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Log.w(TAG, "Logout API call failed: " + t.getMessage());
                    
                    // Clear local data even if API call fails
                    clearLocalData();
                    
                    if (callback != null) {
                        callback.onSuccess(null); // Still consider it successful locally
                    }
                }
            });
        } else {
            // No tokens to logout with, just clear local data
            clearLocalData();
            if (callback != null) {
                callback.onSuccess(null);
            }
        }
    }

    /**
     * Check if user is currently authenticated
     */
    public boolean isAuthenticated() {
        return preferenceManager.isLoggedIn() && 
               preferenceManager.getAccessToken() != null &&
               !preferenceManager.getAccessToken().isEmpty();
    }

    /**
     * Get current user type
     */
    public String getCurrentUserType() {
        return preferenceManager.getUserType();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return preferenceManager.getUserId();
    }

    /**
     * Check if access token is expired and needs refresh
     */
    public boolean isTokenExpired() {
        return preferenceManager.isAccessTokenExpired();
    }

    /**
     * Save authentication data to preferences
     */
    private void saveAuthData(AuthResponse authResponse) {
        preferenceManager.setLoggedIn(true);
        preferenceManager.setAccessToken(authResponse.getAccessToken());
        preferenceManager.setRefreshToken(authResponse.getRefreshToken());
        preferenceManager.setUserType(authResponse.getUserType());
        preferenceManager.setUserId(authResponse.getUserId());
        
        if (authResponse.getAccessTokenExpiresAt() != null) {
            preferenceManager.setAccessTokenExpiry(authResponse.getAccessTokenExpiresAt().getTime());
        }
        
        if (authResponse.getRefreshTokenExpiresAt() != null) {
            preferenceManager.setRefreshTokenExpiry(authResponse.getRefreshTokenExpiresAt().getTime());
        }
    }

    /**
     * Clear all local authentication data
     */
    private void clearLocalData() {
        preferenceManager.clearAuthData();
        // You might also want to clear other local caches here
        // evOwnerDAO.clearAllEVOwners(); // Uncomment if you want to clear user data on logout
    }
}
package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.models.User;
import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.services.api.UserApiService;
import com.ead.zap.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OperatorProfileService to handle operator-specific profile operations
 * Manages operator profile API calls, local caching, and data operations
 */
public class OperatorProfileService {
    private static final String TAG = "OperatorProfileService";
    
    private final Context context;
    private final UserApiService userApiService;
    private final PreferenceManager preferenceManager;
    
    // Cache for profile data
    private User cachedProfile;
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    public OperatorProfileService(Context context) {
        this.context = context.getApplicationContext();
        this.userApiService = NetworkClient.getInstance(context).createService(UserApiService.class);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Profile callback interface for handling profile operations
     */
    public interface OperatorProfileCallback {
        void onSuccess(User profile);
        void onError(String error);
    }

    /**
     * Profile update callback interface
     */
    public interface OperatorProfileUpdateCallback {
        void onSuccess(User updatedProfile);
        void onError(String error);
    }

    /**
     * Get operator profile with caching
     * @param userId The user ID
     * @param callback Callback to handle the response
     */
    public void getOperatorProfile(String userId, OperatorProfileCallback callback) {
        getOperatorProfile(userId, callback, false);
    }

    /**
     * Get operator profile with caching
     * @param userId The user ID  
     * @param callback Callback to handle the response
     * @param forceRefresh Force refresh from server, ignore cache
     */
    public void getOperatorProfile(String userId, OperatorProfileCallback callback, boolean forceRefresh) {
        // Check cache first if not forcing refresh
        if (!forceRefresh && isCacheValid() && cachedProfile != null) {
            Log.d(TAG, "Returning cached operator profile data");
            callback.onSuccess(cachedProfile);
            return;
        }

        Log.d(TAG, "Fetching operator profile for user: " + userId);
        
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<User>> call = userApiService.getUserById("Bearer " + authToken, userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Operator profile loaded successfully");
                        
                        // Update cache
                        cachedProfile = apiResponse.getData();
                        lastFetchTime = System.currentTimeMillis();
                        
                        // Update preferences for quick access
                        saveProfileToPreferences(cachedProfile);
                        
                        callback.onSuccess(cachedProfile);
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "Failed to load profile";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Failed to load profile: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Update operator profile
     * @param userId The user ID
     * @param user The updated user data
     * @param callback Callback to handle the response
     */
    public void updateOperatorProfile(String userId, User user, OperatorProfileUpdateCallback callback) {
        Log.d(TAG, "Updating operator profile for user: " + userId);
        
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }
        
        Call<ApiResponse<User>> call = userApiService.updateUser("Bearer " + authToken, userId, user);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Operator profile updated successfully");
                        
                        // Update cache with new data
                        cachedProfile = apiResponse.getData();
                        lastFetchTime = System.currentTimeMillis();
                        
                        // Update preferences
                        saveProfileToPreferences(cachedProfile);
                        
                        callback.onSuccess(cachedProfile);
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "Failed to update profile";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Failed to update profile: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get cached profile if available and valid
     * @return Cached profile or null if not available/expired
     */
    public User getCachedProfile() {
        if (isCacheValid() && cachedProfile != null) {
            return cachedProfile;
        }
        return null;
    }

    /**
     * Get user display name from cache or preferences
     * @return User display name or "Operator" as fallback
     */
    public String getUserDisplayName() {
        if (cachedProfile != null) {
            return cachedProfile.getDisplayName();
        }
        
        // Fallback to preferences
        String userName = preferenceManager.getUserName();
        
        if (userName != null && !userName.isEmpty()) {
            return userName;
        }
        
        return "Operator";
    }

    /**
     * Get user first name from cache or preferences
     * @return User username as first name or empty string
     */
    public String getUserFirstName() {
        if (cachedProfile != null) {
            return cachedProfile.getUsername();
        }
        
        return preferenceManager.getUserName() != null ? preferenceManager.getUserName() : "";
    }

    /**
     * Clear cached profile data
     */
    public void clearCache() {
        cachedProfile = null;
        lastFetchTime = 0;
        clearProfileFromPreferences();
    }

    /**
     * Check if cached data is still valid
     */
    private boolean isCacheValid() {
        return System.currentTimeMillis() - lastFetchTime < CACHE_DURATION;
    }

    /**
     * Save profile data to preferences for quick access
     */
    private void saveProfileToPreferences(User profile) {
        if (profile != null) {
            // Save basic profile info to preferences for quick access
            preferenceManager.setUserName(profile.getUsername());
            preferenceManager.setUserEmail(profile.getEmail());
        }
    }

    /**
     * Clear profile data from preferences
     */
    private void clearProfileFromPreferences() {
        // Don't clear all preferences, just profile-specific ones
        // Basic auth data should remain for token management
    }

    /**
     * Helper method to get auth token
     */
    private String getAuthToken() {
        return preferenceManager.getAccessToken();
    }

    /**
     * Create user update request from current profile data
     * @param profile Current profile data
     * @return User object for update
     */
    public static User createUpdateRequest(User profile) {
        if (profile == null) return null;
        
        User updateUser = new User();
        updateUser.setId(profile.getId());
        updateUser.setUsername(profile.getUsername());
        updateUser.setEmail(profile.getEmail());
        updateUser.setRole(profile.getRole());
        updateUser.setActive(profile.isActive());
        // Don't include password in updates - backend handles this separately
        
        return updateUser;
    }
}
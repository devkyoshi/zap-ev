package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.models.ProfileUpdateRequest;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.services.api.ProfileApiService;
import com.ead.zap.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileService to handle profile-related operations
 * Manages profile API calls, local caching, and data operations
 */
public class ProfileService {
    private static final String TAG = "ProfileService";
    
    private final Context context;
    private final ProfileApiService profileApiService;
    private final PreferenceManager preferenceManager;
    
    // Cache for profile data
    private ProfileResponse cachedProfile;
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    public ProfileService(Context context) {
        this.context = context.getApplicationContext();
        this.profileApiService = NetworkClient.getInstance(context).createService(ProfileApiService.class);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Profile callback interface for handling profile operations
     */
    public interface ProfileCallback {
        void onSuccess(ProfileResponse profile);
        void onError(String error);
    }

    /**
     * Profile update callback interface
     */
    public interface ProfileUpdateCallback {
        void onSuccess(ProfileResponse updatedProfile);
        void onError(String error);
    }

    /**
     * Get user profile with caching
     * @param userId The user ID
     * @param callback Callback to handle the response
     * @param forceRefresh Force refresh from server, ignore cache
     */
    public void getUserProfile(String userId, ProfileCallback callback, boolean forceRefresh) {
        // Check cache first if not forcing refresh
        if (!forceRefresh && isCacheValid() && cachedProfile != null) {
            Log.d(TAG, "Returning cached profile data");
            callback.onSuccess(cachedProfile);
            return;
        }

        Log.d(TAG, "Fetching profile for user: " + userId);
        
        Call<ApiResponse<ProfileResponse>> call = profileApiService.getProfile(userId);
        call.enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProfileResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Profile fetched successfully");
                        
                        // Update cache
                        cachedProfile = apiResponse.getData();
                        lastFetchTime = System.currentTimeMillis();
                        
                        // Save user info to preferences for quick access
                        saveProfileToPreferences(cachedProfile);
                        
                        callback.onSuccess(cachedProfile);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                                            apiResponse.getMessage() : 
                                            "Failed to load profile";
                        Log.e(TAG, "Profile API error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                } else {
                    String errorMessage = "Profile request failed with code: " + response.code();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get user profile without forcing refresh
     */
    public void getUserProfile(String userId, ProfileCallback callback) {
        getUserProfile(userId, callback, false);
    }

    /**
     * Update user profile
     * @param userId The user ID
     * @param profileUpdate The profile update data
     * @param callback Callback to handle the response
     */
    public void updateUserProfile(String userId, ProfileUpdateRequest profileUpdate, ProfileUpdateCallback callback) {
        Log.d(TAG, "Updating profile for user: " + userId);
        
        Call<ApiResponse<ProfileResponse>> call = profileApiService.updateProfile(userId, profileUpdate);
        call.enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProfileResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Profile updated successfully");
                        
                        // Update cache with new data
                        cachedProfile = apiResponse.getData();
                        lastFetchTime = System.currentTimeMillis();
                        
                        // Update preferences
                        saveProfileToPreferences(cachedProfile);
                        
                        callback.onSuccess(cachedProfile);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                                            apiResponse.getMessage() : 
                                            "Failed to update profile";
                        Log.e(TAG, "Profile update API error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                } else {
                    String errorMessage = "Profile update failed with code: " + response.code();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                String errorMessage = "Network error during profile update: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get cached profile if available and valid
     * @return Cached profile or null if not available/expired
     */
    public ProfileResponse getCachedProfile() {
        if (isCacheValid() && cachedProfile != null) {
            return cachedProfile;
        }
        return null;
    }

    /**
     * Get user display name from cache or preferences
     * @return User display name or "User" as fallback
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
        
        return "User";
    }

    /**
     * Get user first name from cache or preferences
     * @return User first name or empty string
     */
    public String getUserFirstName() {
        if (cachedProfile != null && cachedProfile.getFirstName() != null) {
            return cachedProfile.getFirstName();
        }
        
        String userName = preferenceManager.getUserName();
        if (userName != null && userName.contains(" ")) {
            return userName.split(" ")[0];
        }
        return userName != null ? userName : "";
    }

    /**
     * Clear cached profile data
     */
    public void clearCache() {
        Log.d(TAG, "Clearing profile cache");
        cachedProfile = null;
        lastFetchTime = 0;
        clearProfileFromPreferences();
    }

    /**
     * Check if cached data is still valid
     */
    private boolean isCacheValid() {
        return (System.currentTimeMillis() - lastFetchTime) < CACHE_DURATION;
    }

    /**
     * Save profile data to preferences for quick access
     */
    private void saveProfileToPreferences(ProfileResponse profile) {
        if (profile != null) {
            preferenceManager.saveString("user_first_name", profile.getFirstName() != null ? profile.getFirstName() : "");
            preferenceManager.saveString("user_last_name", profile.getLastName() != null ? profile.getLastName() : "");
            preferenceManager.saveString("user_email", profile.getEmail() != null ? profile.getEmail() : "");
            preferenceManager.saveString("user_phone", profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
            preferenceManager.saveString("user_nic", profile.getNic() != null ? profile.getNic() : "");
            
            Log.d(TAG, "Profile data saved to preferences");
        }
    }

    /**
     * Clear profile data from preferences
     */
    private void clearProfileFromPreferences() {
        preferenceManager.removeKey("user_first_name");
        preferenceManager.removeKey("user_last_name");
        preferenceManager.removeKey("user_email");
        preferenceManager.removeKey("user_phone");
        preferenceManager.removeKey("user_nic");
        
        Log.d(TAG, "Profile data cleared from preferences");
    }

    /**
     * Create profile update request from current profile data
     * @param profile Current profile data
     * @return ProfileUpdateRequest object
     */
    public static ProfileUpdateRequest createUpdateRequest(ProfileResponse profile) {
        if (profile == null) return null;
        
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNic(profile.getNic());
        request.setFirstName(profile.getFirstName());
        request.setLastName(profile.getLastName());
        request.setEmail(profile.getEmail());
        request.setPhoneNumber(profile.getPhoneNumber());
        request.setVehicleDetails(profile.getVehicleDetails());
        
        return request;
    }
}
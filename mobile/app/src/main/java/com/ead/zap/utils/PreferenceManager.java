package com.ead.zap.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Enhanced PreferenceManager to handle authentication data and app preferences
 */
public class PreferenceManager {
    private static final String PREF_NAME = "ZapPrefs";
    
    // General app preferences
    private static final String KEY_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String KEY_IS_LOGGED_IN = "IsLoggedIn";
    
    // Authentication tokens
    private static final String KEY_ACCESS_TOKEN = "AccessToken";
    private static final String KEY_REFRESH_TOKEN = "RefreshToken";
    private static final String KEY_ACCESS_TOKEN_EXPIRY = "AccessTokenExpiry";
    private static final String KEY_REFRESH_TOKEN_EXPIRY = "RefreshTokenExpiry";
    
    // User information
    private static final String KEY_USER_ID = "UserId";
    private static final String KEY_USER_TYPE = "UserType";
    private static final String KEY_USER_EMAIL = "UserEmail";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_USER_NIC = "UserNIC"; // For EV Owners
    
    // App settings
    private static final String KEY_SELECTED_LANGUAGE = "SelectedLanguage";
    private static final String KEY_NOTIFICATION_ENABLED = "NotificationEnabled";
    private static final String KEY_LOCATION_PERMISSION_GRANTED = "LocationPermissionGranted";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ============ General App Preferences ============
    
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(KEY_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(KEY_FIRST_TIME_LAUNCH, true);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // ============ Authentication Tokens ============
    
    public void setAccessToken(String token) {
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
    }

    public String getAccessToken() {
        return pref.getString(KEY_ACCESS_TOKEN, null);
    }

    public void setRefreshToken(String token) {
        editor.putString(KEY_REFRESH_TOKEN, token);
        editor.apply();
    }

    public String getRefreshToken() {
        return pref.getString(KEY_REFRESH_TOKEN, null);
    }

    public void setAccessTokenExpiry(long expiryTime) {
        editor.putLong(KEY_ACCESS_TOKEN_EXPIRY, expiryTime);
        editor.apply();
    }

    public long getAccessTokenExpiry() {
        return pref.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0);
    }

    public void setRefreshTokenExpiry(long expiryTime) {
        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, expiryTime);
        editor.apply();
    }

    public long getRefreshTokenExpiry() {
        return pref.getLong(KEY_REFRESH_TOKEN_EXPIRY, 0);
    }

    /**
     * Check if access token is expired
     */
    public boolean isAccessTokenExpired() {
        long expiryTime = getAccessTokenExpiry();
        if (expiryTime == 0) return true;
        return System.currentTimeMillis() > expiryTime;
    }

    /**
     * Check if refresh token is expired
     */
    public boolean isRefreshTokenExpired() {
        long expiryTime = getRefreshTokenExpiry();
        if (expiryTime == 0) return true;
        return System.currentTimeMillis() > expiryTime;
    }

    // ============ User Information ============
    
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public void setUserType(String userType) {
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }

    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, null);
    }

    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    public void setUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    public void setUserNIC(String nic) {
        editor.putString(KEY_USER_NIC, nic);
        editor.apply();
    }

    public String getUserNIC() {
        return pref.getString(KEY_USER_NIC, null);
    }

    // ============ Extended Profile Information ============
    
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
    
    public String getString(String key, String defaultValue) {
        return pref.getString(key, defaultValue);
    }
    
    public void removeKey(String key) {
        editor.remove(key);
        editor.apply();
    }

    /**
     * Check if current user is EV Owner
     */
    public boolean isEVOwner() {
        String userType = getUserType();
        return "EVOwner".equalsIgnoreCase(userType);
    }

    /**
     * Check if current user is BackOffice
     */
    public boolean isBackOffice() {
        String userType = getUserType();
        return "BackOffice".equalsIgnoreCase(userType);
    }

    /**
     * Check if current user is Station Operator
     */
    public boolean isStationOperator() {
        String userType = getUserType();
        return "StationOperator".equalsIgnoreCase(userType);
    }

    // ============ App Settings ============
    
    public void setSelectedLanguage(String language) {
        editor.putString(KEY_SELECTED_LANGUAGE, language);
        editor.apply();
    }

    public String getSelectedLanguage() {
        return pref.getString(KEY_SELECTED_LANGUAGE, "en");
    }

    public void setNotificationEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isNotificationEnabled() {
        return pref.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setLocationPermissionGranted(boolean granted) {
        editor.putBoolean(KEY_LOCATION_PERMISSION_GRANTED, granted);
        editor.apply();
    }

    public boolean isLocationPermissionGranted() {
        return pref.getBoolean(KEY_LOCATION_PERMISSION_GRANTED, false);
    }

    // ============ Clear Data Methods ============
    
    /**
     * Clear all authentication data
     */
    public void clearAuthData() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_ACCESS_TOKEN_EXPIRY);
        editor.remove(KEY_REFRESH_TOKEN_EXPIRY);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_TYPE);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_NIC);
        editor.apply();
    }

    /**
     * Clear all preferences (complete app reset)
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // ============ Utility Methods ============
    
    /**
     * Get formatted access token for API calls
     */
    public String getBearerToken() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Check if authentication is valid (logged in and token not expired)
     */
    public boolean isAuthenticationValid() {
        return isLoggedIn() && 
               getAccessToken() != null && 
               !getAccessToken().isEmpty() && 
               !isAccessTokenExpired();
    }

    /**
     * Check if refresh is possible (refresh token exists and not expired)
     */
    public boolean canRefreshToken() {
        return getRefreshToken() != null && 
               !getRefreshToken().isEmpty() && 
               !isRefreshTokenExpired();
    }
}

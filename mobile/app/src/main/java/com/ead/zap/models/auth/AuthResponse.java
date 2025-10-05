package com.ead.zap.models.auth;

import java.util.Date;

/**
 * Authentication response model containing tokens and user info
 */
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userType;
    private String role;
    private String userId;
    private Date accessTokenExpiresAt;
    private Date refreshTokenExpiresAt;

    public AuthResponse() {}

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(Date accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public Date getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(Date refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    /**
     * Check if access token is expired
     */
    public boolean isAccessTokenExpired() {
        if (accessTokenExpiresAt == null) return true;
        return new Date().after(accessTokenExpiresAt);
    }

    /**
     * Check if refresh token is expired
     */
    public boolean isRefreshTokenExpired() {
        if (refreshTokenExpiresAt == null) return true;
        return new Date().after(refreshTokenExpiresAt);
    }

    /**
     * Check if user is EV Owner
     */
    public boolean isEVOwner() {
        // Check both role and userType for compatibility
        boolean result = "EVOwner".equalsIgnoreCase(role) || "EVOwner".equalsIgnoreCase(userType);
        System.out.println("DEBUG: isEVOwner() - role: '" + role + "', userType: '" + userType + "' -> " + result);
        return result;
    }

    /**
     * Check if user is BackOffice (should not use mobile app)
     */
    public boolean isBackOffice() {
        // BackOffice users should use web interface, not mobile app
        return "BackOffice".equalsIgnoreCase(role);
    }

    /**
     * Check if user is StationOperator
     */
    public boolean isStationOperator() {
        boolean result = "StationOperator".equalsIgnoreCase(role);
        System.out.println("DEBUG: isStationOperator() - role: '" + role + "', userType: '" + userType + "' -> " + result);
        return result;
    }
}
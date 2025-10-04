package com.ead.zap.models;

import java.util.Date;

/**
 * User model for Station Operator users (BackOffice uses web interface)
 */
public class User {
    private String id;
    private String username;
    private String email;
    private UserRole role;
    private boolean isActive;
    private Date lastLogin;
    private Date createdAt;
    private Date updatedAt;

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * User role enumeration
     */
    public enum UserRole {
        BACKOFFICE(1, "BackOffice"),
        STATIONOPERATOR(2, "StationOperator");

        private final int value;
        private final String displayName;

        UserRole(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public int getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static UserRole fromValue(int value) {
            for (UserRole role : UserRole.values()) {
                if (role.value == value) {
                    return role;
                }
            }
            return null;
        }

        public static UserRole fromDisplayName(String displayName) {
            for (UserRole role : UserRole.values()) {
                if (role.displayName.equalsIgnoreCase(displayName)) {
                    return role;
                }
            }
            return null;
        }
    }
}
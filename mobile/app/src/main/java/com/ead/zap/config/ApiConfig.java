package com.ead.zap.config;

/**
 * API Configuration class to manage base URLs and endpoints
 * This prevents hardcoding URLs throughout the application
 */
public class ApiConfig {
    // Base URL configurations
    private static final String LOCAL_BASE_URL = "https://b850b7fd7058.ngrok-free.app/api/";
    private static final String DEVELOPMENT_BASE_URL = "https://your-dev-server.com/api/";
    private static final String PRODUCTION_BASE_URL = "https://your-production-server.com/api/";
    
    // Current environment - change this based on your deployment
    public enum Environment {
        LOCAL,
        DEVELOPMENT,
        PRODUCTION
    }
    
    // Set your current environment here
    private static final Environment CURRENT_ENVIRONMENT = Environment.LOCAL;
    
    /**
     * Get the base URL based on current environment
     * @return Base URL string
     */
    public static String getBaseUrl() {
        switch (CURRENT_ENVIRONMENT) {
            case DEVELOPMENT:
                return DEVELOPMENT_BASE_URL;
            case PRODUCTION:
                return PRODUCTION_BASE_URL;
            default:
                return LOCAL_BASE_URL;
        }
    }
    
    // Authentication endpoints
    public static final class Auth {
        public static final String LOGIN = "auth/login";
        public static final String LOGIN_EV_OWNER = "auth/login/evowner";
        public static final String REFRESH = "auth/refresh";
        public static final String LOGOUT = "auth/logout";
    }
    
    // User endpoints (BackOffice only - not used in mobile app)
    public static final class Users {
        public static final String REGISTER = "users/register";
        public static final String BASE = "users";
        public static final String STATUS = "users/{id}/status";
    }
    
    // EV Owner endpoints
    public static final class EVOwners {
        public static final String REGISTER = "evowners/register";
        public static final String BASE = "evowners/{id}";
        public static final String PROFILE = "evowners/{id}";
        public static final String UPDATE_PROFILE = "evowners/{id}/profile";
        public static final String DEACTIVATED = "evowners/deactivated";
        public static final String REACTIVATE = "evowners/{id}/reactivate";
        public static final String DASHBOARD = "evowners/dashboard/{nic}";
    }
    
    // Charging Station endpoints
    public static final class ChargingStations {
        public static final String BASE = "chargingstations";
        public static final String NEARBY = "chargingstations/nearby";
        public static final String SLOTS = "chargingstations/{id}/slots";
        public static final String STATUS = "chargingstations/{id}/status";
    }
    
    // Booking endpoints
    public static final class Bookings {
        public static final String BASE = "bookings";
        public static final String EV_OWNER = "bookings/evowner/{nic}";
        public static final String APPROVE = "bookings/{id}/approve";
        public static final String START = "bookings/{id}/start";
        public static final String COMPLETE = "bookings/{id}/complete";
        public static final String VERIFY_QR = "bookings/verify-qr";
        public static final String UPCOMING = "bookings/evowner/{nic}/upcoming";
        public static final String HISTORY = "bookings/evowner/{nic}/history";
    }
    
    // Request timeout configurations (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Authentication token constants
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Content type constants
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    /**
     * Check if we're running in debug mode (local or development)
     * @return true if debug mode, false for production
     */
    public static boolean isDebugMode() {
        return CURRENT_ENVIRONMENT == Environment.LOCAL || 
               CURRENT_ENVIRONMENT == Environment.DEVELOPMENT;
    }
}
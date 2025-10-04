# ZAP EV - Mobile Authentication Setup

This document explains the authentication system setup for the ZAP EV mobile application.

## 📋 Overview

The mobile app now includes a complete authentication system that connects to the backend API with the following features:

- **EV Owner Authentication** - Login/Registration for electric vehicle owners
- **User Authentication** - Login for BackOffice and Station Operator users  
- **Token Management** - Automatic token refresh and secure storage
- **Local Database** - SQLite for offline data caching
- **Network Layer** - Retrofit with OkHttp for API communication

## 🏗️ Architecture

### Core Components

1. **ApiConfig** - Centralized API endpoint and configuration management
2. **NetworkClient** - Retrofit client with interceptors for authentication
3. **AuthService** - Main authentication service handling all auth operations
4. **AuthApiService** - Retrofit interface defining API endpoints
5. **PreferenceManager** - Enhanced to handle tokens and user data
6. **Database Layer** - SQLite with DAO pattern for local storage

### Authentication Models

- `LoginRequest` / `EVOwnerLoginRequest` - Login request DTOs
- `AuthResponse` - Authentication response with tokens
- `EVOwnerRegistrationRequest` - Registration request DTO
- `User` / `EVOwner` - User entity models

## 🚀 Getting Started

### 1. Backend Configuration

Update the base URL in `ApiConfig.java`:

```java
// Change this to your backend server URL
private static final String LOCAL_BASE_URL = "https://your-backend-url.com/api/";
```

### 2. Build Dependencies

The following dependencies have been added to `build.gradle`:

```gradle
// Networking
implementation libs.retrofit
implementation libs.retrofit.gson
implementation libs.okhttp
implementation libs.okhttp.logging
implementation libs.gson

// Database
implementation libs.room.runtime
annotationProcessor libs.room.compiler
```

### 3. Android Permissions

Added to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 💻 Usage Examples

### EV Owner Login

```java
AuthService authService = new AuthService(context);

authService.loginEVOwner("123456789V", "password", new AuthService.AuthCallback() {
    @Override
    public void onSuccess(AuthResponse response) {
        // Login successful - redirect to main screen
        Intent intent = new Intent(context, EVOwnerMain.class);
        startActivity(intent);
    }
    
    @Override
    public void onError(String error) {
        // Show error message
        Toast.makeText(context, "Login failed: " + error, Toast.LENGTH_LONG).show();
    }
});
```

### Regular User Login

```java
authService.login("username", "password", new AuthService.AuthCallback() {
    @Override
    public void onSuccess(AuthResponse response) {
        if (response.isStationOperator()) {
            // Redirect to Station Operator dashboard
            Intent intent = new Intent(context, StationOperatorMain.class);
            startActivity(intent);
        } else if (response.isBackOffice()) {
            // Redirect to BackOffice dashboard
            // Navigate accordingly
        }
    }
    
    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

### EV Owner Registration

```java
EVOwnerRegistrationRequest request = new EVOwnerRegistrationRequest();
request.setNic("123456789V");
request.setFirstName("John");
request.setLastName("Doe");
request.setEmail("john@example.com");
request.setPhoneNumber("+94771234567");
request.setPassword("securePassword123");

// Add vehicle details
List<EVOwnerRegistrationRequest.VehicleDetail> vehicles = new ArrayList<>();
// ... add vehicles

authService.registerEVOwner(request, new AuthService.RegistrationCallback() {
    @Override
    public void onSuccess(EVOwner evOwner) {
        // Registration successful
    }
    
    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

### Automatic Token Management

```java
// Check authentication status
if (authService.isAuthenticated()) {
    if (authService.isTokenExpired()) {
        // Automatically refresh token
        authService.refreshToken(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                // Token refreshed - continue with app
            }
            
            @Override
            public void onError(String error) {
                // Refresh failed - redirect to login
            }
        });
    } else {
        // Token valid - proceed to main app
    }
} else {
    // Not authenticated - show login
}
```

### Logout

```java
authService.logout(new AuthService.AuthCallback() {
    @Override
    public void onSuccess(AuthResponse response) {
        // Logout successful - redirect to login
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    
    @Override
    public void onError(String error) {
        // Handle error (still redirect to login)
    }
});
```

## 🔐 Security Features

### Token Management
- Access tokens stored securely in SharedPreferences
- Automatic token refresh before expiration
- Secure token cleanup on logout

### Network Security
- HTTPS enforcement for production
- Request/response logging only in debug mode
- Authentication headers automatically added to API calls

### Local Storage
- User data cached locally for offline access
- SQLite database with proper foreign key constraints
- Data encryption can be added for sensitive information

## 🛠️ Configuration

### Environment Setup

The `ApiConfig` class supports multiple environments:

```java
public enum Environment {
    LOCAL,      // For local development
    DEVELOPMENT,// For dev server
    PRODUCTION  // For production server
}
```

Change the `CURRENT_ENVIRONMENT` constant based on your deployment target.

### Network Timeouts

Configure network timeouts in `ApiConfig`:

```java
public static final int CONNECT_TIMEOUT = 30; // seconds
public static final int READ_TIMEOUT = 30;
public static final int WRITE_TIMEOUT = 30;
```

## 📱 Integration with Existing Activities

### In Login Activities

Initialize `AuthService` and call appropriate login methods based on user type.

### In Main Activities

Check authentication status in `onCreate()` or `onResume()`:

```java
AuthService authService = new AuthService(this);
if (!authService.isAuthenticated()) {
    // Redirect to login
    Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
    finish();
}
```

### In API Calls

The `NetworkClient` automatically adds authentication headers to API requests. For endpoints that require authentication, just make the API call normally.

## 🔧 Troubleshooting

### Common Issues

1. **Network Security Exception**: Make sure your backend server supports HTTPS or add network security config for HTTP in development.

2. **Authentication Failed**: Check if the backend server is running and accessible from your device/emulator.

3. **Token Refresh Failed**: Ensure refresh tokens are properly stored and the refresh endpoint is working.

### Debug Logging

Enable detailed logging by ensuring `ApiConfig.isDebugMode()` returns `true` in development builds.

## 🚀 Next Steps

The authentication foundation is now complete. You can:

1. Integrate with existing login/registration activities
2. Add other API endpoints (Bookings, Charging Stations, etc.)
3. Implement offline-first architecture using the local database
4. Add push notification token management
5. Implement biometric authentication for enhanced security

## 📞 Support

For questions or issues, refer to the backend API documentation at `EVChargingStationAPI/SETUP.md` for endpoint details.
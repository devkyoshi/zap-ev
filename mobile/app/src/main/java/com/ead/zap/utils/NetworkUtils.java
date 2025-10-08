package com.ead.zap.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.ead.zap.config.ApiConfig;

/**
 * Network utility class for connection management and testing
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * Check if device has internet connection
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        
        return false;
    }

    /**
     * Interface for connection test callback
     */
    public interface ConnectionTestCallback {
        void onConnectionResult(boolean isReachable, String workingUrl);
    }

    /**
     * Test backend server connectivity asynchronously
     */
    public static void testBackendConnection(Context context, ConnectionTestCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onConnectionResult(false, null);
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Reset cached URL to force fresh detection
//                    ApiConfig.resetCache();
                    
                    // Get the base URL (this will trigger auto-detection)
                    String baseUrl = ApiConfig.getBaseUrl();
                    
                    // Test if the swagger endpoint is reachable
                    String testUrl = baseUrl.replace("/api/", "/swagger");
                    java.net.URL url = new java.net.URL(testUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("HEAD");
                    
                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    
                    if (responseCode < 400) {
                        Log.d(TAG, "Backend server reachable at: " + baseUrl);
                        return baseUrl;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Backend connection test failed: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String workingUrl) {
                callback.onConnectionResult(workingUrl != null, workingUrl);
            }
        }.execute();
    }

    /**
     * Get a user-friendly connection status message
     */
    public static void getConnectionStatusMessage(Context context, ConnectionStatusCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onStatusMessage("No internet connection", false);
            return;
        }

        testBackendConnection(context, new ConnectionTestCallback() {
            @Override
            public void onConnectionResult(boolean isReachable, String workingUrl) {
                if (isReachable && workingUrl != null) {
                    String serverInfo = extractServerInfo(workingUrl);
                    callback.onStatusMessage("Connected to backend server " + serverInfo, true);
                } else {
                    callback.onStatusMessage("Cannot reach backend server. Please check if it's running.", false);
                }
            }
        });
    }

    /**
     * Interface for connection status callback
     */
    public interface ConnectionStatusCallback {
        void onStatusMessage(String message, boolean isConnected);
    }

    /**
     * Extract readable server info from URL
     */
    private static String extractServerInfo(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String host = urlObj.getHost();
            int port = urlObj.getPort();
            
            if ("10.0.2.2".equals(host)) {
                return "(Android Emulator)";
            } else if (host.startsWith("192.168.")) {
                return "(" + host + ":" + port + ")";
            } else {
                return "(" + host + ")";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get device's IP address for debugging
     */
    public static String getDeviceIpAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = 
                java.net.NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                
                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress() || address instanceof java.net.Inet6Address) continue;
                    
                    return address.getHostAddress();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting device IP: " + e.getMessage());
        }
        return "Unknown";
    }
}
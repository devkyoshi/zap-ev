package com.ead.zap.network;

import android.content.Context;
import com.ead.zap.config.ApiConfig;
import com.ead.zap.utils.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Network client configuration for Retrofit
 * Handles HTTP client setup, interceptors, and service creation
 */
public class NetworkClient {
    private static NetworkClient instance;
    private final Retrofit retrofit;
    private final PreferenceManager preferenceManager;

    private NetworkClient(Context context) {
        this.preferenceManager = new PreferenceManager(context);

        // Configure Gson for date handling
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        // Build OkHttp client with interceptors
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Add authentication interceptor
        httpClient.addInterceptor(new AuthInterceptor());
        
        // Add response interceptor to handle common errors
        httpClient.addInterceptor(new ResponseInterceptor());

        // Add logging interceptor for debugging (only in debug mode)
        if (ApiConfig.isDebugMode()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
        }

        // Build Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.getBaseUrl())
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * Get singleton instance of NetworkClient
     */
    public static synchronized NetworkClient getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkClient(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Create API service instances
     */
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    /**
     * Authentication interceptor to add Bearer token to requests
     */
    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            // Skip auth for login and registration endpoints
            String url = original.url().toString();
            if (isAuthSkippedEndpoint(url)) {
                return chain.proceed(original);
            }

            // Get access token from preferences
            String accessToken = preferenceManager.getAccessToken();
            
            if (accessToken != null && !accessToken.isEmpty()) {
                Request.Builder requestBuilder = original.newBuilder()
                        .header(ApiConfig.AUTHORIZATION_HEADER, 
                               ApiConfig.BEARER_PREFIX + accessToken)
                        .header("Content-Type", ApiConfig.CONTENT_TYPE_JSON);
                
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }

            return chain.proceed(original);
        }

        /**
         * Check if endpoint should skip authentication
         */
        private boolean isAuthSkippedEndpoint(String url) {
            return url.contains("/auth/login") || 
                   url.contains("/auth/refresh") || 
                   url.contains("/evowners/register") ||
                   url.contains("/users/register");
        }
    }

    /**
     * Response interceptor to handle common HTTP errors
     */
    private class ResponseInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            
            // Handle 401 Unauthorized - token expired
            if (response.code() == 401) {
                // Clear stored tokens
                preferenceManager.clearAuthData();
                // You might want to redirect to login screen here
                // This could be done via a broadcast or callback
            }
            
            return response;
        }
    }
}
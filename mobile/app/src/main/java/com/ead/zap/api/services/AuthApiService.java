package com.ead.zap.api.services;

import com.ead.zap.config.ApiConfig;
import com.ead.zap.models.auth.*;
import com.ead.zap.models.EVOwner;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

/**
 * Authentication API service interface using Retrofit
 * Contains all authentication-related endpoints
 */
public interface AuthApiService {

    /**
     * Login for regular users (BackOffice/StationOperator)
     * POST /api/auth/login
     */
    @POST(ApiConfig.Auth.LOGIN)
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Login for EV Owners
     * POST /api/auth/login/evowner
     */
    @POST(ApiConfig.Auth.LOGIN_EV_OWNER)
    Call<AuthResponse> loginEVOwner(@Body EVOwnerLoginRequest request);

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @POST(ApiConfig.Auth.REFRESH)
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    /**
     * Logout user
     * POST /api/auth/logout
     */
    @POST(ApiConfig.Auth.LOGOUT)
    Call<Void> logout(
        @Header("Authorization") String authToken,
        @Body LogoutRequest request
    );

    /**
     * Register new EV Owner
     * POST /api/evowners/register
     */
    @POST(ApiConfig.EVOwners.REGISTER)
    Call<EVOwner> registerEVOwner(@Body EVOwnerRegistrationRequest request);
}
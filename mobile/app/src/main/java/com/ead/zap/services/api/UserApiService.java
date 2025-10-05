package com.ead.zap.services.api;

import com.ead.zap.models.User;
import com.ead.zap.models.common.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * UserApiService interface for operator user-related API endpoints
 * Provides methods to get and update Station Operator profile information
 */
public interface UserApiService {

    /**
     * Get User profile by ID
     * GET /api/users/{id}
     * @param authToken Authorization header with Bearer token
     * @param id The User ID
     * @return Call containing the user response wrapped in ApiResponse
     */
    @GET("users/{id}")
    Call<ApiResponse<User>> getUserById(
        @Header("Authorization") String authToken,
        @Path("id") String id
    );

    /**
     * Update User profile
     * PUT /api/users/{id}
     * @param authToken Authorization header with Bearer token
     * @param id The User ID
     * @param user The updated user data
     * @return Call containing the updated user response wrapped in ApiResponse
     */
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(
        @Header("Authorization") String authToken,
        @Path("id") String id, 
        @Body User user
    );
}
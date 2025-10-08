package com.ead.zap.services.api;

import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.models.ProfileUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import static com.ead.zap.config.ApiConfig.EVOwners.PROFILE;
import static com.ead.zap.config.ApiConfig.EVOwners.UPDATE_PROFILE;

/**
 * ProfileApiService interface for profile-related API endpoints
 * Provides methods to get and update EV Owner profile information
 */
public interface ProfileApiService {

    /**
     * Get EV Owner profile by ID
     * @param id The EV Owner ID
     * @return Call containing the profile response wrapped in ApiResponse
     */
    @GET(PROFILE)
    Call<ApiResponse<ProfileResponse>> getProfile(@Path("id") String id);

    /**
     * Update EV Owner profile
     * @param id The EV Owner ID
     * @param profileUpdate The profile update request containing new data
     * @return Call containing the updated profile response wrapped in ApiResponse
     */
    @PUT(UPDATE_PROFILE)
    Call<ApiResponse<ProfileResponse>> updateProfile(@Path("id") String id, @Body ProfileUpdateRequest profileUpdate);
}
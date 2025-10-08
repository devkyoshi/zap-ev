package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.api.services.BookingApiService;
import com.ead.zap.models.Booking;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BookingService to handle booking-related operations
 * Manages booking API calls and data operations
 */
public class BookingService {
    private static final String TAG = "BookingService";
    
    private final Context context;
    private final BookingApiService bookingApiService;
    private final PreferenceManager preferenceManager;

    public BookingService(Context context) {
        this.context = context.getApplicationContext();
        this.bookingApiService = NetworkClient.getInstance(context).createService(BookingApiService.class);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Interface for handling booking responses
     */
    public interface BookingCallback {
        void onSuccess(BookingApiService.BookingResponseDTO booking);
        void onError(String error);
    }

    /**
     * Interface for handling booking list responses
     */
    public interface BookingListCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String error);
    }

    /**
     * Interface for handling boolean responses
     */
    public interface BooleanCallback {
        void onSuccess(boolean result);
        void onError(String error);
    }

    /**
     * Create a new booking
     */
    public void createBooking(String stationId, Date reservationDateTime, int durationMinutes, 
                             String notes, BookingCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        // Format date to ISO 8601
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = isoFormat.format(reservationDateTime);

        BookingApiService.CreateBookingRequest request = new BookingApiService.CreateBookingRequest(
                stationId, formattedDate, durationMinutes, notes
        );

        Call<ApiResponse<BookingApiService.BookingResponseDTO>> call = 
                bookingApiService.createBooking("Bearer " + authToken, request);

        call.enqueue(new Callback<ApiResponse<BookingApiService.BookingResponseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, 
                                 Response<ApiResponse<BookingApiService.BookingResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BookingApiService.BookingResponseDTO> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to create booking: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Create booking failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get all bookings for current EV owner (including pending, approved, etc.)
     */
    public void getAllBookings(BookingListCallback callback) {
        String authToken = getAuthToken();
        String nic = preferenceManager.getUserNIC();
        
        Log.d(TAG, "Getting all bookings - AuthToken: " + (authToken != null ? "Present" : "NULL") + 
                   ", NIC: " + (nic != null ? nic : "NULL"));
        
        if (authToken == null) {
            Log.e(TAG, "No auth token available");
            callback.onError("Not authenticated - no token");
            return;
        }
        
        if (nic == null) {
            Log.e(TAG, "No NIC available");
            callback.onError("Not authenticated - no user NIC");
            return;
        }

        Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call = 
                bookingApiService.getBookingsByEVOwner("Bearer " + authToken, nic);

        call.enqueue(new Callback<ApiResponse<List<BookingApiService.BookingResponseDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, 
                                 Response<ApiResponse<List<BookingApiService.BookingResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<BookingApiService.BookingResponseDTO>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> bookings = convertToBookingList(apiResponse.getData());
                        Log.d(TAG, "Successfully loaded all bookings: " + bookings.size());
                        callback.onSuccess(bookings);
                    } else {
                        Log.e(TAG, "API error: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Failed to get all bookings: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += " - " + response.message();
                        }
                    }
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, Throwable t) {
                Log.e(TAG, "Get all bookings failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get upcoming bookings for current EV owner
     */
    public void getUpcomingBookings(BookingListCallback callback) {
        // Debug authentication status
        debugAuthStatus();
        
        String authToken = getAuthToken();
        String nic = preferenceManager.getUserNIC();
        
        Log.d(TAG, "Getting upcoming bookings - AuthToken: " + (authToken != null ? "Present" : "NULL") + 
                   ", NIC: " + (nic != null ? nic : "NULL"));
        
        if (authToken == null) {
            Log.e(TAG, "No auth token available");
            callback.onError("Not authenticated - no token");
            return;
        }
        
        if (nic == null) {
            Log.e(TAG, "No NIC available");
            callback.onError("Not authenticated - no user NIC");
            return;
        }

        Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call = 
                bookingApiService.getUpcomingBookings("Bearer " + authToken, nic);

        call.enqueue(new Callback<ApiResponse<List<BookingApiService.BookingResponseDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, 
                                 Response<ApiResponse<List<BookingApiService.BookingResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<BookingApiService.BookingResponseDTO>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> bookings = convertToBookingList(apiResponse.getData());
                        callback.onSuccess(bookings);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Failed to get upcoming bookings: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += " - " + response.message();
                        }
                    }
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, Throwable t) {
                Log.e(TAG, "Get upcoming bookings failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get booking history for current EV owner
     */
    public void getBookingHistory(BookingListCallback callback) {
        String authToken = getAuthToken();
        String nic = preferenceManager.getUserNIC();
        
        Log.d(TAG, "Getting booking history - AuthToken: " + (authToken != null ? "Present" : "NULL") + 
                   ", NIC: " + (nic != null ? nic : "NULL"));
        
        if (authToken == null) {
            Log.e(TAG, "No auth token available");
            callback.onError("Not authenticated - no token");
            return;
        }
        
        if (nic == null) {
            Log.e(TAG, "No NIC available");
            callback.onError("Not authenticated - no user NIC");
            return;
        }

        Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call = 
                bookingApiService.getBookingHistory("Bearer " + authToken, nic);

        call.enqueue(new Callback<ApiResponse<List<BookingApiService.BookingResponseDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, 
                                 Response<ApiResponse<List<BookingApiService.BookingResponseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<BookingApiService.BookingResponseDTO>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> bookings = convertToBookingList(apiResponse.getData());
                        callback.onSuccess(bookings);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Failed to get booking history: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += " - " + response.message();
                        }
                    }
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingApiService.BookingResponseDTO>>> call, Throwable t) {
                Log.e(TAG, "Get booking history failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Update an existing booking
     */
    public void updateBooking(String bookingId, Date reservationDateTime, Integer durationMinutes, 
                             String notes, BookingCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        // Format date to ISO 8601
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = reservationDateTime != null ? isoFormat.format(reservationDateTime) : null;

        BookingApiService.UpdateBookingRequest request = new BookingApiService.UpdateBookingRequest(
                formattedDate, durationMinutes, notes
        );

        Call<ApiResponse<BookingApiService.BookingResponseDTO>> call = 
                bookingApiService.updateBooking("Bearer " + authToken, bookingId, request);

        call.enqueue(new Callback<ApiResponse<BookingApiService.BookingResponseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, 
                                 Response<ApiResponse<BookingApiService.BookingResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BookingApiService.BookingResponseDTO> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to update booking: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Update booking failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Cancel a booking
     */
    public void cancelBooking(String bookingId, BooleanCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<Boolean>> call = 
                bookingApiService.cancelBooking("Bearer " + authToken, bookingId);

        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, 
                                 Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to cancel booking: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "Cancel booking failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Helper method to get auth token
     */
    private String getAuthToken() {
        String token = preferenceManager.getAccessToken();
        Log.d(TAG, "Auth token status: " + (token != null ? "Present (length: " + token.length() + ")" : "NULL"));
        
        // Check if token is expired
        if (token != null && preferenceManager.isAccessTokenExpired()) {
            Log.w(TAG, "Access token is expired!");
        }
        
        return token;
    }

    /**
     * Debug method to check authentication status
     */
    public void debugAuthStatus() {
        Log.d(TAG, "=== Authentication Debug Info ===");
        Log.d(TAG, "Is Logged In: " + preferenceManager.isLoggedIn());
        Log.d(TAG, "Access Token: " + (preferenceManager.getAccessToken() != null ? "Present" : "NULL"));
        Log.d(TAG, "User NIC: " + preferenceManager.getUserNIC());
        Log.d(TAG, "User Type: " + preferenceManager.getUserType());
        Log.d(TAG, "Is Access Token Expired: " + preferenceManager.isAccessTokenExpired());
        Log.d(TAG, "Can Refresh Token: " + preferenceManager.canRefreshToken());
        Log.d(TAG, "==================================");
    }

    /**
     * Convert BookingResponseDTO list to Booking list
     */
    private List<Booking> convertToBookingList(List<BookingApiService.BookingResponseDTO> dtoList) {
        List<Booking> bookings = new ArrayList<>();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        
        for (BookingApiService.BookingResponseDTO dto : dtoList) {
            try {
                Booking booking = new Booking();
                booking.setBookingId(dto.getId());
                booking.setUserId(dto.getEvOwnerNIC());
                booking.setStationId(dto.getChargingStationId());
                booking.setStationName(dto.getChargingStationName());
                booking.setDuration(dto.getDurationMinutes());
                booking.setTotalCost(dto.getTotalAmount());
                // Handle status - convert from string (which might be a number from backend)
                Log.d(TAG, "Converting status from API: '" + dto.getStatus() + "'");
                booking.setStatusFromString(dto.getStatus());
                Log.d(TAG, "Converted to enum: " + booking.getStatus().getDisplayName());
                booking.setQrCode(dto.getQrCode());
                
                // Parse dates
                Date reservationDateTime = isoFormat.parse(dto.getReservationDateTime());
                booking.setReservationDate(reservationDateTime);
                booking.setReservationTime(reservationDateTime);
                
                if (dto.getCreatedAt() != null) {
                    booking.setCreatedAt(isoFormat.parse(dto.getCreatedAt()));
                }
                
                bookings.add(booking);
            } catch (Exception e) {
                Log.e(TAG, "Error converting booking DTO: " + e.getMessage(), e);
            }
        }
        
        return bookings;
    }
}
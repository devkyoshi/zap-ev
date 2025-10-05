package com.ead.zap.services;

import android.content.Context;
import android.util.Log;

import com.ead.zap.api.services.BookingApiService;
import com.ead.zap.models.Booking;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.network.NetworkClient;
import com.ead.zap.utils.PreferenceManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service class for handling Station Operator specific operations
 * Manages QR verification, booking management, and session operations
 */
public class OperatorService {
    private static final String TAG = "OperatorService";
    
    private final Context context;
    private final BookingApiService bookingApiService;
    private final PreferenceManager preferenceManager;

    public OperatorService(Context context) {
        this.context = context.getApplicationContext();
        this.bookingApiService = NetworkClient.getInstance(context).createService(BookingApiService.class);
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Callback interfaces for async operations
     */
    public interface QRVerificationCallback {
        void onSuccess(BookingVerificationResult verificationResult);
        void onError(String error);
    }

    public interface BookingOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface BookingHistoryCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String error);
    }

    /**
     * QR Verification result model
     */
    public static class BookingVerificationResult {
        private String bookingId;
        private String customerName;
        private String customerNIC;
        private String stationId;
        private String slotNumber;
        private String startTime;
        private String endTime;
        private String status;
        private boolean isValid;
        private String message;

        // Constructors
        public BookingVerificationResult() {}

        public BookingVerificationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        // Getters and Setters
        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerNIC() { return customerNIC; }
        public void setCustomerNIC(String customerNIC) { this.customerNIC = customerNIC; }

        public String getStationId() { return stationId; }
        public void setStationId(String stationId) { this.stationId = stationId; }

        public String getSlotNumber() { return slotNumber; }
        public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        // Additional fields for booking details
        private int duration;
        private double totalCost;

        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }

        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    }

    /**
     * Verify QR code for booking
     */
    public void verifyQRCode(String qrCode, QRVerificationCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        // Create QR verification request
        BookingApiService.QRVerificationRequest request = new BookingApiService.QRVerificationRequest(qrCode);

        Call<ApiResponse<BookingApiService.BookingResponseDTO>> call = 
                bookingApiService.verifyQRCode("Bearer " + authToken, request);
        
        call.enqueue(new Callback<ApiResponse<BookingApiService.BookingResponseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, 
                                 Response<ApiResponse<BookingApiService.BookingResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BookingApiService.BookingResponseDTO> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Convert booking data to our result model
                        BookingApiService.BookingResponseDTO booking = apiResponse.getData();
                        BookingVerificationResult result = new BookingVerificationResult(true, "QR code verified successfully");
                        result.setBookingId(booking.getId());
                        result.setCustomerName(booking.getEvOwnerNIC()); // Use NIC as customer identifier
                        result.setStationId(booking.getChargingStationId());
                        result.setStartTime(booking.getReservationDateTime());
                        result.setDuration(booking.getDurationMinutes());
                        result.setTotalCost(booking.getTotalAmount());
                        
                        Log.d(TAG, "QR verification successful for booking: " + booking.getId());
                        callback.onSuccess(result);
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "QR verification failed";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "QR verification failed: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingApiService.BookingResponseDTO>> call, Throwable t) {
                String error = "Network error verifying QR code: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Start a booking session
     */
    public void startBookingSession(String bookingId, BookingOperationCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<Object>> call = bookingApiService.startBooking("Bearer " + authToken, bookingId);
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null ? 
                                        apiResponse.getMessage() : "Session started successfully";
                        Log.d(TAG, message);
                        callback.onSuccess(message);
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "Failed to start session";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Failed to start session: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Complete a booking session
     */
    public void completeBookingSession(String bookingId, BookingOperationCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<Object>> call = bookingApiService.completeBooking("Bearer " + authToken, bookingId);
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String message = apiResponse.getMessage() != null ? 
                                        apiResponse.getMessage() : "Session completed successfully";
                        Log.d(TAG, message);
                        callback.onSuccess(message);
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "Failed to complete session";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Failed to complete session: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Get booking history for station operator
     */
    public void getSessionHistory(BookingHistoryCallback callback) {
        String authToken = getAuthToken();
        if (authToken == null) {
            callback.onError("Not authenticated");
            return;
        }

        Call<ApiResponse<List<Booking>>> call = bookingApiService.getAllBookings("Bearer " + authToken);
        
        call.enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, 
                                 Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Session history loaded successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        String error = apiResponse.getMessage() != null ? 
                                      apiResponse.getMessage() : "Failed to load history";
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                } else {
                    String error = "Failed to load history: " + response.message();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Helper method to get auth token
     */
    private String getAuthToken() {
        return preferenceManager.getAccessToken();
    }


}
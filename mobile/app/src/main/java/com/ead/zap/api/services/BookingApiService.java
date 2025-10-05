package com.ead.zap.api.services;

import com.ead.zap.config.ApiConfig;
import com.ead.zap.models.common.ApiResponse;
import com.ead.zap.models.Booking;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Booking API service interface using Retrofit
 * Contains all booking-related endpoints
 */
public interface BookingApiService {

    /**
     * Create a new booking
     * POST /api/bookings
     */
    @POST(ApiConfig.Bookings.BASE)
    Call<ApiResponse<BookingResponseDTO>> createBooking(
        @Header("Authorization") String authToken,
        @Body CreateBookingRequest request
    );

    /**
     * Get all bookings for a specific EV owner
     * GET /api/bookings/evowner/{nic}
     */
    @GET(ApiConfig.Bookings.EV_OWNER)
    Call<ApiResponse<List<BookingResponseDTO>>> getBookingsByEVOwner(
        @Header("Authorization") String authToken,
        @Path("nic") String nic
    );

    /**
     * Get upcoming bookings for an EV owner
     * GET /api/bookings/evowner/{nic}/upcoming
     */
    @GET(ApiConfig.Bookings.UPCOMING)
    Call<ApiResponse<List<BookingResponseDTO>>> getUpcomingBookings(
        @Header("Authorization") String authToken,
        @Path("nic") String nic
    );

    /**
     * Get booking history for an EV owner
     * GET /api/bookings/evowner/{nic}/history
     */
    @GET(ApiConfig.Bookings.HISTORY)
    Call<ApiResponse<List<BookingResponseDTO>>> getBookingHistory(
        @Header("Authorization") String authToken,
        @Path("nic") String nic
    );

    /**
     * Update an existing booking
     * PUT /api/bookings/{id}
     */
    @PUT(ApiConfig.Bookings.BASE + "/{id}")
    Call<ApiResponse<BookingResponseDTO>> updateBooking(
        @Header("Authorization") String authToken,
        @Path("id") String bookingId,
        @Body UpdateBookingRequest request
    );

    /**
     * Cancel a booking
     * DELETE /api/bookings/{id}
     */
    @DELETE(ApiConfig.Bookings.BASE + "/{id}")
    Call<ApiResponse<Boolean>> cancelBooking(
        @Header("Authorization") String authToken,
        @Path("id") String bookingId
    );

    /**
     * Get all bookings (for operators/admin)
     * GET /api/bookings
     */
    @GET(ApiConfig.Bookings.BASE)
    Call<ApiResponse<List<Booking>>> getAllBookings(
        @Header("Authorization") String authToken
    );

    /**
     * Start a booking session
     * PATCH /api/bookings/{id}/start
     */
    @PATCH(ApiConfig.Bookings.BASE + "/{id}/start")
    Call<ApiResponse<Object>> startBooking(
        @Header("Authorization") String authToken,
        @Path("id") String bookingId
    );

    /**
     * Complete a booking session
     * PATCH /api/bookings/{id}/complete
     */
    @PATCH(ApiConfig.Bookings.BASE + "/{id}/complete")
    Call<ApiResponse<Object>> completeBooking(
        @Header("Authorization") String authToken,
        @Path("id") String bookingId
    );

    /**
     * Verify QR code for booking
     * POST /api/bookings/verify-qr
     */
    @POST(ApiConfig.Bookings.VERIFY_QR)
    Call<ApiResponse<BookingResponseDTO>> verifyQRCode(
        @Header("Authorization") String authToken,
        @Body QRVerificationRequest request
    );

    /**
     * DTOs for API requests and responses
     */
    class CreateBookingRequest {
        private String chargingStationId;
        private String reservationDateTime; // ISO 8601 format
        private int durationMinutes;
        private String notes;

        public CreateBookingRequest() {}

        public CreateBookingRequest(String chargingStationId, String reservationDateTime, 
                                   int durationMinutes, String notes) {
            this.chargingStationId = chargingStationId;
            this.reservationDateTime = reservationDateTime;
            this.durationMinutes = durationMinutes;
            this.notes = notes;
        }

        // Getters and setters
        public String getChargingStationId() { return chargingStationId; }
        public void setChargingStationId(String chargingStationId) { this.chargingStationId = chargingStationId; }

        public String getReservationDateTime() { return reservationDateTime; }
        public void setReservationDateTime(String reservationDateTime) { this.reservationDateTime = reservationDateTime; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    class UpdateBookingRequest {
        private String reservationDateTime; // ISO 8601 format
        private Integer durationMinutes;
        private String notes;

        public UpdateBookingRequest() {}

        public UpdateBookingRequest(String reservationDateTime, Integer durationMinutes, String notes) {
            this.reservationDateTime = reservationDateTime;
            this.durationMinutes = durationMinutes;
            this.notes = notes;
        }

        // Getters and setters
        public String getReservationDateTime() { return reservationDateTime; }
        public void setReservationDateTime(String reservationDateTime) { this.reservationDateTime = reservationDateTime; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    class BookingResponseDTO {
        private String id;
        private String evOwnerNIC;
        private String chargingStationId;
        private String chargingStationName;
        private String reservationDateTime;
        private int durationMinutes;
        private String status; // PENDING, APPROVED, CANCELLED, COMPLETED
        private double totalAmount;
        private String qrCode;
        private String createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEvOwnerNIC() { return evOwnerNIC; }
        public void setEvOwnerNIC(String evOwnerNIC) { this.evOwnerNIC = evOwnerNIC; }

        public String getChargingStationId() { return chargingStationId; }
        public void setChargingStationId(String chargingStationId) { this.chargingStationId = chargingStationId; }

        public String getChargingStationName() { return chargingStationName; }
        public void setChargingStationName(String chargingStationName) { this.chargingStationName = chargingStationName; }

        public String getReservationDateTime() { return reservationDateTime; }
        public void setReservationDateTime(String reservationDateTime) { this.reservationDateTime = reservationDateTime; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    class QRVerificationRequest {
        private String qrCode;

        public QRVerificationRequest() {}

        public QRVerificationRequest(String qrCode) {
            this.qrCode = qrCode;
        }

        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    }

    class QRVerificationResponseDTO {
        private boolean isValid;
        private String bookingId;
        private String message;

        // Getters and setters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }

        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
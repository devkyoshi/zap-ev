package com.ead.zap.ui.owner.modals;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ead.zap.R;
import com.ead.zap.models.Booking;
import com.ead.zap.models.BookingStatus;
import com.ead.zap.services.BookingService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CancelReservationActivity extends AppCompatActivity {

    private TextView tvStationName, tvStationAddress, tvBookingDate, tvBookingTime,
                    tvDuration, tvTotalCost, tvBookingId, tvStatus, tvCancellationPolicy;
    private Button btnBackToBookings, btnCancelReservation;
    private Booking booking;
    private BookingService bookingService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_reservation);

        bookingService = new BookingService(this);
        
        setupToolbar();
        initViews();
        loadBookingData();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cancel Reservation");
        }
    }

    private void initViews() {
        tvStationName = findViewById(R.id.tv_station_name);
        tvStationAddress = findViewById(R.id.tv_station_address);
        tvBookingDate = findViewById(R.id.tv_booking_date);
        tvBookingTime = findViewById(R.id.tv_booking_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvStatus = findViewById(R.id.tv_status);
        tvCancellationPolicy = findViewById(R.id.tv_cancellation_policy);
        btnBackToBookings = findViewById(R.id.btn_back_to_bookings);
        btnCancelReservation = findViewById(R.id.btn_cancel_reservation);
    }

    private void loadBookingData() {
        booking = (Booking) getIntent().getSerializableExtra("booking");
        
        if (booking == null) {
            Toast.makeText(this, "Error loading booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if booking can be cancelled (at least 12 hours before)
        Calendar bookingTime = Calendar.getInstance();
        bookingTime.setTime(booking.getReservationTime());
        
        Calendar minCancellationTime = Calendar.getInstance();
        minCancellationTime.add(Calendar.HOUR, 12);

        boolean canCancel = bookingTime.after(minCancellationTime) && 
                           booking.getStatus().canBeCancelled();

        // Populate booking information
        tvStationName.setText(booking.getStationName());
        tvStationAddress.setText(booking.getStationAddress());
        tvBookingDate.setText(dateFormat.format(booking.getReservationDate()));
        tvBookingTime.setText(timeFormat.format(booking.getReservationTime()));
        tvDuration.setText(String.format(Locale.getDefault(), "%d minutes", booking.getDuration()));
        tvTotalCost.setText(String.format(Locale.getDefault(), "LKR %.2f", booking.getTotalCost()));
        tvBookingId.setText(booking.getBookingId() != null ? booking.getBookingId() : "N/A");
        tvStatus.setText(booking.getStatusString());

        // Set status color
        switch (booking.getStatus()) {
            case APPROVED:
                tvStatus.setTextColor(getColor(R.color.primary_light));
                break;
            case PENDING:
                tvStatus.setTextColor(getColor(R.color.amber_600));
                break;
            case CANCELLED:
                tvStatus.setTextColor(getColor(R.color.red_600));
                break;
            case COMPLETED:
                tvStatus.setTextColor(getColor(R.color.gray_600));
                break;
            default:
                tvStatus.setTextColor(getColor(R.color.black));
                break;
        }

        // Show cancellation policy and enable/disable cancel button
        if (canCancel) {
            tvCancellationPolicy.setText("You can cancel this reservation free of charge at least 12 hours before the scheduled time.");
            btnCancelReservation.setEnabled(true);
        } else {
            if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
                tvCancellationPolicy.setText("This reservation has already been cancelled.");
            } else if (BookingStatus.COMPLETED.equals(booking.getStatus())) {
                tvCancellationPolicy.setText("This reservation has been completed and cannot be cancelled.");
            } else {
                tvCancellationPolicy.setText("Cannot cancel reservation less than 12 hours before the scheduled time.");
            }
            btnCancelReservation.setEnabled(false);
            btnCancelReservation.setAlpha(0.5f);
        }
    }

    private void setupClickListeners() {
        btnBackToBookings.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnCancelReservation.setOnClickListener(v -> showCancellationDialog());
    }

    private void showCancellationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Cancellation")
               .setMessage("Are you sure you want to cancel this reservation?\n\n" +
                          "Station: " + booking.getStationName() + "\n" +
                          "Date: " + dateFormat.format(booking.getReservationDate()) + "\n" +
                          "Time: " + timeFormat.format(booking.getReservationTime()) + "\n\n" +
                          "This action cannot be undone.")
               .setPositiveButton("Yes, Cancel Reservation", (dialog, which) -> {
                   cancelReservation();
               })
               .setNegativeButton("Keep Reservation", (dialog, which) -> {
                   dialog.dismiss();
               })
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }

    private void cancelReservation() {
        // Disable button to prevent multiple clicks
        btnCancelReservation.setEnabled(false);
        btnCancelReservation.setText("Cancelling...");

        bookingService.cancelBooking(
            booking.getBookingId(),
            new BookingService.BooleanCallback() {
                @Override
                public void onSuccess(boolean result) {
                    runOnUiThread(() -> {
                        // Update local booking object
                        booking.setStatus(BookingStatus.CANCELLED);
                        booking.setUpdatedAt(new Date());

                        // Return the cancelled booking to the calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("cancelled_booking", booking);
                        setResult(RESULT_OK, resultIntent);
                        
                        // Show success dialog
                        showCancellationSuccessDialog();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(CancelReservationActivity.this, 
                            "Failed to cancel reservation: " + error, Toast.LENGTH_LONG).show();
                        
                        // Re-enable button
                        btnCancelReservation.setEnabled(true);
                        btnCancelReservation.setText("Cancel Reservation");
                    });
                }
            }
        );
    }

    private void showCancellationSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reservation Cancelled")
               .setMessage("Your reservation has been successfully cancelled.\n\n" +
                          "Booking ID: " + booking.getBookingId() + "\n\n" +
                          "A confirmation email will be sent to your registered email address.")
               .setPositiveButton("Back to Bookings", (dialog, which) -> {
                   finish();
               })
               .setNeutralButton("Make New Booking", (dialog, which) -> {
                   // Navigate to booking creation
                   Intent intent = new Intent(this, CreateBookingActivity.class);
                   startActivity(intent);
                   finish();
               })
               .setCancelable(false)
               .setIcon(android.R.drawable.ic_dialog_info)
               .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
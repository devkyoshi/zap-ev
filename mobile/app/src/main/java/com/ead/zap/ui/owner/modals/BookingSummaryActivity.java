package com.ead.zap.ui.owner.modals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ead.zap.R;
import com.ead.zap.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BookingSummaryActivity extends AppCompatActivity {

    private TextView tvStationName, tvStationAddress, tvBookingDate, tvBookingTime, 
                    tvDuration, tvChargingRate, tvTotalCost, tvVehicleInfo;
    private Button btnCancel, btnConfirm;
    private Booking booking;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

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
            getSupportActionBar().setTitle("Booking Summary");
        }
    }

    private void initViews() {
        tvStationName = findViewById(R.id.tv_station_name);
        tvStationAddress = findViewById(R.id.tv_station_address);
        tvBookingDate = findViewById(R.id.tv_booking_date);
        tvBookingTime = findViewById(R.id.tv_booking_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvChargingRate = findViewById(R.id.tv_charging_rate);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvVehicleInfo = findViewById(R.id.tv_vehicle_info);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void loadBookingData() {
        booking = (Booking) getIntent().getSerializableExtra("booking");
        
        if (booking == null) {
            Toast.makeText(this, "Error loading booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate the views with booking data
        tvStationName.setText(booking.getStationName());
        tvStationAddress.setText(booking.getStationAddress());
        tvBookingDate.setText(dateFormat.format(booking.getReservationDate()));
        tvBookingTime.setText(timeFormat.format(booking.getReservationTime()));
        tvDuration.setText(String.format(Locale.getDefault(), "%d minutes", booking.getDuration()));
        
        // Calculate charging rate per minute
        double ratePerMinute = booking.getTotalCost() / booking.getDuration();
        tvChargingRate.setText(String.format(Locale.getDefault(), "LKR %.2f per minute", ratePerMinute));
        tvTotalCost.setText(String.format(Locale.getDefault(), "LKR %.2f", booking.getTotalCost()));
        
        // Mock vehicle info - this should come from selected vehicle
        tvVehicleInfo.setText("Tesla Model 3 - ABC-1234");
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnConfirm.setOnClickListener(v -> confirmBooking());

        // Handle back button
        if (getSupportActionBar() != null) {
            findViewById(android.R.id.home).setOnClickListener(v -> onBackPressed());
        }
    }

    private void confirmBooking() {
        // Here you would typically make an API call to confirm the booking
        // For now, we'll simulate a successful booking confirmation
        
        booking.setStatusFromString("PENDING");
        booking.setBookingId("BOOK" + System.currentTimeMillis());
        
        // Navigate to QR Code screen after successful booking
        Intent qrIntent = new Intent(this, QRCodeActivity.class);
        qrIntent.putExtra("booking", booking);
        startActivity(qrIntent);
        
        // Set result and finish this activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("confirmed_booking", booking);
        setResult(RESULT_OK, resultIntent);
        finish();
        
        Toast.makeText(this, "Booking confirmed successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
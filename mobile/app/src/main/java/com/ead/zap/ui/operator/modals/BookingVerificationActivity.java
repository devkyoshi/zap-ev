package com.ead.zap.ui.operator.modals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.ead.zap.R;
import com.ead.zap.services.OperatorService;

public class BookingVerificationActivity extends AppCompatActivity {

    private TextView tvBookingId, tvCustomerName, tvStationId, tvSlotNumber, tvStartTime, tvEndTime;
    private Button btnStartSession, btnReject;
    private CardView cardBookingDetails;
    private OperatorService operatorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_verification);

        // Initialize services
        operatorService = new OperatorService(this);

        initViews();
        setupToolbar();
        loadBookingData();
        setupClickListeners();
    }

    private void initViews() {
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvStationId = findViewById(R.id.tv_station_id);
        tvSlotNumber = findViewById(R.id.tv_slot_number);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        btnStartSession = findViewById(R.id.btn_start_session);
        btnReject = findViewById(R.id.btn_reject);
        cardBookingDetails = findViewById(R.id.card_booking_details);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking Verification");
        }
    }

    private void loadBookingData() {
        Intent intent = getIntent();
        String bookingId = intent.getStringExtra("booking_id");
        String customerName = intent.getStringExtra("customer_name");
        String stationId = intent.getStringExtra("station_id");
        String slotNumber = intent.getStringExtra("slot_number");
        String startTime = intent.getStringExtra("start_time");
        String endTime = intent.getStringExtra("end_time");

        tvBookingId.setText(bookingId != null ? bookingId : "N/A");
        tvCustomerName.setText(customerName != null ? customerName : "N/A");
        tvStationId.setText(stationId != null ? stationId : "N/A");
        tvSlotNumber.setText(slotNumber != null ? slotNumber : "N/A");
        tvStartTime.setText(startTime != null ? startTime : "N/A");
        tvEndTime.setText(endTime != null ? endTime : "N/A");
    }

    private void setupClickListeners() {
        btnStartSession.setOnClickListener(v -> {
            startBookingSession();
        });

        btnReject.setOnClickListener(v -> {
            // Handle rejection logic here
            Toast.makeText(this, "Booking rejected", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startBookingSession() {
        String bookingId = tvBookingId.getText().toString();
        
        // Disable button to prevent multiple clicks
        btnStartSession.setEnabled(false);
        btnStartSession.setText("Starting...");
        
        operatorService.startBookingSession(bookingId, new OperatorService.BookingOperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    // Navigate to finalize session activity
                    Intent intent = new Intent(BookingVerificationActivity.this, FinalizeSessionActivity.class);
                    intent.putExtra("booking_id", bookingId);
                    intent.putExtra("customer_name", tvCustomerName.getText().toString());
                    intent.putExtra("station_id", tvStationId.getText().toString());
                    intent.putExtra("slot_number", tvSlotNumber.getText().toString());
                    intent.putExtra("start_time", tvStartTime.getText().toString());
                    intent.putExtra("end_time", tvEndTime.getText().toString());
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingVerificationActivity.this, 
                        "Failed to start session: " + error, Toast.LENGTH_LONG).show();
                    
                    // Re-enable button
                    btnStartSession.setEnabled(true);
                    btnStartSession.setText("Start Session");
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
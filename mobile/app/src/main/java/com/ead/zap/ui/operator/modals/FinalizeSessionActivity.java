package com.ead.zap.ui.operator.modals;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.ead.zap.R;
import com.ead.zap.services.OperatorService;
import com.ead.zap.ui.operator.StationOperatorMain;

public class FinalizeSessionActivity extends AppCompatActivity {

    private TextView tvBookingId, tvCustomerName, tvStationId, tvSlotNumber;
    private TextView tvStartTime, tvCurrentTime, tvEnergyDelivered, tvSessionStatus;
    private Button btnFinalizeSession;
    private ProgressBar progressBarCharging;
    private CardView cardSessionDetails;
    private OperatorService operatorService;

    private Handler handler = new Handler();
    private int simulatedProgress = 0;
    private boolean isSessionActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalize_session);

        // Initialize services
        operatorService = new OperatorService(this);

        initViews();
        setupToolbar();
        loadSessionData();
        setupClickListeners();
        startSimulatedCharging();
    }

    private void initViews() {
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvStationId = findViewById(R.id.tv_station_id);
        tvSlotNumber = findViewById(R.id.tv_slot_number);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvEnergyDelivered = findViewById(R.id.tv_energy_delivered);
        tvSessionStatus = findViewById(R.id.tv_session_status);
        btnFinalizeSession = findViewById(R.id.btn_finalize_session);
        progressBarCharging = findViewById(R.id.progress_bar_charging);
        cardSessionDetails = findViewById(R.id.card_session_details);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Active Charging Session");
        }
    }

    private void loadSessionData() {
        Intent intent = getIntent();
        String bookingId = intent.getStringExtra("booking_id");
        String customerName = intent.getStringExtra("customer_name");
        String stationId = intent.getStringExtra("station_id");
        String slotNumber = intent.getStringExtra("slot_number");
        String startTime = intent.getStringExtra("start_time");

        tvBookingId.setText(bookingId != null ? bookingId : "N/A");
        tvCustomerName.setText(customerName != null ? customerName : "N/A");
        tvStationId.setText(stationId != null ? stationId : "N/A");
        tvSlotNumber.setText(slotNumber != null ? slotNumber : "N/A");
        tvStartTime.setText(startTime != null ? startTime : "N/A");

        // Set initial values
        tvCurrentTime.setText("2:15 PM");
        tvEnergyDelivered.setText("0.0 kWh");
        tvSessionStatus.setText("Charging in Progress");
        progressBarCharging.setProgress(0);
    }

    private void setupClickListeners() {
        btnFinalizeSession.setOnClickListener(v -> {
            finalizeChargingSession();
        });
    }

    private void startSimulatedCharging() {
        final Runnable chargingSimulator = new Runnable() {
            @Override
            public void run() {
                if (isSessionActive && simulatedProgress < 100) {
                    simulatedProgress += 2;
                    progressBarCharging.setProgress(simulatedProgress);
                    
                    // Update energy delivered (simulate 0.5 kWh per 10% progress)
                    double energyDelivered = (simulatedProgress / 10.0) * 0.5;
                    tvEnergyDelivered.setText(String.format("%.1f kWh", energyDelivered));
                    
                    // Update current time (simulate time passing)
                    int minutesPassed = simulatedProgress / 4; // 2.5 minutes per 10%
                    int currentHour = 2;
                    int currentMinute = 15 + minutesPassed;
                    
                    if (currentMinute >= 60) {
                        currentHour += currentMinute / 60;
                        currentMinute = currentMinute % 60;
                    }
                    
                    String timeFormat = currentHour + ":" + String.format("%02d", currentMinute) + " PM";
                    tvCurrentTime.setText(timeFormat);
                    
                    // Continue simulation
                    handler.postDelayed(this, 500); // Update every 500ms
                } else if (simulatedProgress >= 100) {
                    tvSessionStatus.setText("Charging Complete");
                    btnFinalizeSession.setText("Complete Session");
                }
            }
        };
        
        handler.post(chargingSimulator);
    }

    private void finalizeChargingSession() {
        isSessionActive = false;
        String bookingId = tvBookingId.getText().toString();
        
        // Disable button to prevent multiple clicks
        btnFinalizeSession.setEnabled(false);
        btnFinalizeSession.setText("Completing...");
        
        operatorService.completeBookingSession(bookingId, new OperatorService.BookingOperationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(FinalizeSessionActivity.this, 
                        "Session completed successfully!", Toast.LENGTH_LONG).show();
                    
                    // Navigate back to main operator screen
                    Intent intent = new Intent(FinalizeSessionActivity.this, StationOperatorMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(FinalizeSessionActivity.this, 
                        "Failed to complete session: " + error, Toast.LENGTH_LONG).show();
                    
                    // Re-enable button
                    btnFinalizeSession.setEnabled(true);
                    btnFinalizeSession.setText("Complete Session");
                    isSessionActive = true;
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSessionActive = false;
        handler.removeCallbacksAndMessages(null);
    }
}
package com.ead.zap.ui.owner.modals;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ead.zap.R;
import com.ead.zap.adapters.VehicleSelectionAdapter;
import com.ead.zap.models.Booking;
import com.ead.zap.models.Vehicle;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateBookingActivity extends AppCompatActivity {
    private static final int STATION_SELECTION_REQUEST = 1001;

    private TextInputEditText etBookingDate, etBookingTime;
    private AutoCompleteTextView etDuration;
    private TextView tvSelectedStation, tvStationAddress, tvChargingRate, tvEstimatedCost;
    private Button btnCancel, btnContinue;
    private RecyclerView rvVehicles;
    private VehicleSelectionAdapter vehicleAdapter;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    // Selected station data
    private String selectedStationId;
    private String selectedStationName;
    private String selectedStationAddress;
    private double selectedStationPrice;
    private int selectedStationAvailableSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        initViews();
        setupDateAndTimePickers();
        setupVehicleList();
        setupClickListeners();
    }

    private void initViews() {
        etBookingDate = findViewById(R.id.et_booking_date);
        etBookingTime = findViewById(R.id.et_booking_time);
        etDuration = findViewById(R.id.et_duration);
        tvSelectedStation = findViewById(R.id.tv_selected_station);
        tvStationAddress = findViewById(R.id.tv_station_address);
        tvChargingRate = findViewById(R.id.tv_charging_rate);
        tvEstimatedCost = findViewById(R.id.tv_estimated_cost);
        btnCancel = findViewById(R.id.btn_cancel);
        btnContinue = findViewById(R.id.btn_continue);
        rvVehicles = findViewById(R.id.rv_vehicles);
    }

    private void setupDateAndTimePickers() {
        // Date Picker
        etBookingDate.setOnClickListener(v -> showDatePicker());

        // Time Picker
        etBookingTime.setOnClickListener(v -> showTimePicker());

        // Duration dropdown setup
        setupDurationDropdown();
    }

    private void setupDurationDropdown() {
        String[] durationOptions = {
            "30 minutes",
            "45 minutes", 
            "60 minutes",
            "90 minutes",
            "120 minutes",
            "180 minutes"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            durationOptions
        );
        
        etDuration.setAdapter(adapter);
        etDuration.setOnItemClickListener((parent, view, position, id) -> {
            // Extract duration value from selected text
            String selected = durationOptions[position];
            // You can add logic here to update cost calculation
        });
    }

    private void showDatePicker() {
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 7); // Max 7 days in future

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etBookingDate.setText(dateFormat.format(selectedDate.getTime()));
                    validateBookingDateTime();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);
                    etBookingTime.setText(timeFormat.format(selectedDate.getTime()));
                    validateBookingDateTime();
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void validateBookingDateTime() {
        Calendar now = Calendar.getInstance();
        Calendar minAllowedTime = Calendar.getInstance();
        minAllowedTime.add(Calendar.HOUR, 12); // Must book at least 12 hours in advance

        if (selectedDate.before(minAllowedTime)) {
            // Show error message
            etBookingTime.setError("Must book at least 12 hours in advance");
            btnContinue.setEnabled(false);
        } else {
            etBookingTime.setError(null);
            btnContinue.setEnabled(true);
        }
    }

    private void setupVehicleList() {
        List<Vehicle> vehicles = getVehicles(); // This would come from database
        vehicleAdapter = new VehicleSelectionAdapter(vehicles);
        rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        rvVehicles.setAdapter(vehicleAdapter);
    }

    private List<Vehicle> getVehicles() {
        // Mock data - replace with actual database call
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("1", "Tesla Model 3", "ABC-1234", "Electric"));
        vehicles.add(new Vehicle("2", "Nissan Leaf", "XYZ-5678", "Electric"));
        return vehicles;
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            if (validateForm()) {
                proceedToSummary();
            }
        });

        // Station selection click listener
        findViewById(R.id.station_selection_card).setOnClickListener(v -> {
            Intent intent = new Intent(CreateBookingActivity.this, StationSelectionActivity.class);
            startActivityForResult(intent, STATION_SELECTION_REQUEST);
        });
    }

//    private boolean validateForm() {
//        boolean isValid = true;
//
//        if (etBookingDate.getText().toString().isEmpty()) {
//            etBookingDate.setError("Please select a date");
//            isValid = false;
//        }
//
//        if (etBookingTime.getText().toString().isEmpty()) {
//            etBookingTime.setError("Please select a time");
//            isValid = false;
//        }
//
//        if (etDuration.getText().toString().isEmpty()) {
//            etDuration.setError("Please select duration");
//            isValid = false;
//        }
//
//        return isValid;
//    }

    private void proceedToSummary() {
        Vehicle selectedVehicle = vehicleAdapter.getSelectedVehicle();
        if (selectedVehicle == null) {
            // Make sure a vehicle is selected
            return;
        }

        // Extract input values
        String bookingDateStr = etBookingDate.getText().toString();
        String bookingTimeStr = etBookingTime.getText().toString();
        String durationStr = etDuration.getText().toString();

        int duration = Integer.parseInt(durationStr); // minutes
        double chargingRate = 50.0; // example rate per minute
        double totalCost = duration * chargingRate;

        // Use selected station data
        String stationId = selectedStationId;
        String stationName = selectedStationName;
        String stationAddress = selectedStationAddress;

        // Create a Booking object
        Booking booking = new Booking(
                "USER001", // Replace with actual logged-in userId
                stationId,
                stationName,
                stationAddress,
                selectedDate.getTime(),  // reservation date
                selectedDate.getTime(),  // reservation time (same Calendar)
                duration,
                totalCost
        );

        // Pass booking to summary activity
        Intent intent = new Intent(this, BookingSummaryActivity.class);
        intent.putExtra("booking", booking);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == STATION_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get selected station data
            selectedStationId = data.getStringExtra("selected_station_id");
            selectedStationName = data.getStringExtra("selected_station_name");
            selectedStationAddress = data.getStringExtra("selected_station_address");
            selectedStationPrice = data.getDoubleExtra("selected_station_price", 0.0);
            selectedStationAvailableSlots = data.getIntExtra("selected_station_available_slots", 0);
            
            // Update UI with selected station
            updateSelectedStationUI();
        }
    }

    private void updateSelectedStationUI() {
        if (selectedStationName != null) {
            tvSelectedStation.setText(selectedStationName);
            tvStationAddress.setText(selectedStationAddress);
            tvChargingRate.setText(String.format("Rs. %.2f / hour", selectedStationPrice));
            
            // Enable continue button if station is selected
            updateContinueButton();
        }
    }

    private void updateContinueButton() {
        boolean canContinue = selectedStationId != null && 
                             !etBookingDate.getText().toString().isEmpty() &&
                             !etBookingTime.getText().toString().isEmpty() &&
                             !etDuration.getText().toString().isEmpty();
        btnContinue.setEnabled(canContinue);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (selectedStationId == null || selectedStationId.isEmpty()) {
            // Show error that station must be selected
            tvSelectedStation.setError("Please select a charging station");
            isValid = false;
        }

        if (etBookingDate.getText().toString().isEmpty()) {
            etBookingDate.setError("Please select a date");
            isValid = false;
        }

        if (etBookingTime.getText().toString().isEmpty()) {
            etBookingTime.setError("Please select a time");
            isValid = false;
        }

        if (etDuration.getText().toString().isEmpty()) {
            etDuration.setError("Please select duration");
            isValid = false;
        }

        return isValid;
    }

}
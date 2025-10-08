package com.ead.zap.ui.owner.modals;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ead.zap.R;
import com.ead.zap.adapters.VehicleSelectionAdapter;
import com.ead.zap.models.Booking;
import com.ead.zap.models.Vehicle;
import com.ead.zap.models.VehicleDetail;
import com.ead.zap.models.ChargingStation;
import com.ead.zap.services.BookingService;
import com.ead.zap.services.ChargingStationService;
import com.ead.zap.services.ProfileService;
import com.ead.zap.utils.PreferenceManager;
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

    // Services
    private BookingService bookingService;
    private ChargingStationService stationService;
    private ProfileService profileService;
    private PreferenceManager preferenceManager;

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

        initServices();
        initViews();
        setupDateAndTimePickers();
        setupVehicleList();
        setupClickListeners();
    }

    private void initServices() {
        bookingService = new BookingService(this);
        stationService = new ChargingStationService(this);
        profileService = new ProfileService(this);
        preferenceManager = new PreferenceManager(this);
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
            updateCostCalculation();
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
        // Initialize empty adapter first
        vehicleAdapter = new VehicleSelectionAdapter(new ArrayList<>());
        rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        rvVehicles.setAdapter(vehicleAdapter);
        
        // Load vehicles from profile
        loadVehiclesFromProfile();
    }

    private void loadVehiclesFromProfile() {
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        profileService.getUserProfile(userId, new ProfileService.ProfileCallback() {
            @Override
            public void onSuccess(com.ead.zap.models.ProfileResponse profile) {
                if (profile.getVehicleDetails() != null && !profile.getVehicleDetails().isEmpty()) {
                    // Convert VehicleDetail to Vehicle objects
                    List<Vehicle> vehicles = new ArrayList<>();
                    for (VehicleDetail vehicleDetail : profile.getVehicleDetails()) {
                        Vehicle vehicle = new Vehicle(
                            String.valueOf(vehicles.size() + 1), // Simple ID
                            vehicleDetail.getMake() + " " + vehicleDetail.getModel(),
                            vehicleDetail.getLicensePlate(),
                            "Electric" // Default type for all vehicles in EV app
                        );
                        vehicles.add(vehicle);
                    }
                    
                    // Update adapter with real vehicles
                    runOnUiThread(() -> {
                        vehicleAdapter.updateVehicles(vehicles);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBookingActivity.this, 
                            "No vehicles found. Please add vehicles in your profile first.", 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateBookingActivity.this, 
                        "Failed to load vehicles: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            if (validateForm()) {
                proceedToSummary();
            }
        });

        // Station selection click listener - try both the card and its contents
        View stationSelectionCard = findViewById(R.id.station_selection_card);
        View tvSelectedStation = findViewById(R.id.tv_selected_station);
        View tvStationAddress = findViewById(R.id.tv_station_address);
        
        // Define the click action
        View.OnClickListener stationClickListener = v -> {
            android.util.Log.d("CreateBookingActivity", "Station selection clicked from: " + v.getClass().getSimpleName());
            Toast.makeText(this, "Opening station selection...", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(CreateBookingActivity.this, StationSelectionActivity.class);
            startActivityForResult(intent, STATION_SELECTION_REQUEST);
        };
        
        if (stationSelectionCard != null) {
            android.util.Log.d("CreateBookingActivity", "Setting click listener on station selection card");
            stationSelectionCard.setOnClickListener(stationClickListener);
        } else {
            android.util.Log.e("CreateBookingActivity", "Station selection card NOT FOUND!");
        }
        
        // Also set click listeners on the text views as backup
        if (tvSelectedStation != null) {
            android.util.Log.d("CreateBookingActivity", "Setting click listener on selected station text");
            tvSelectedStation.setOnClickListener(stationClickListener);
        }
        
        if (tvStationAddress != null) {
            android.util.Log.d("CreateBookingActivity", "Setting click listener on station address text");
            tvStationAddress.setOnClickListener(stationClickListener);
        }
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
            Toast.makeText(this, "Please select a vehicle", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract input values
        String durationStr = etDuration.getText().toString();
        if (durationStr.isEmpty()) {
            Toast.makeText(this, "Please select duration", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse duration from string (e.g., "90 minutes" -> 90)
        int duration;
        try {
            duration = Integer.parseInt(durationStr.split(" ")[0]);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        btnContinue.setEnabled(false);
        btnContinue.setText("Creating booking...");

        // Create booking via API
        bookingService.createBooking(
            selectedStationId,
            selectedDate.getTime(),
            duration,
            "", // No notes for now
            new BookingService.BookingCallback() {
                @Override
                public void onSuccess(com.ead.zap.api.services.BookingApiService.BookingResponseDTO bookingResponse) {
                    runOnUiThread(() -> {
                        // Convert API response to Booking object for summary
                        Booking booking = new Booking();
                        booking.setBookingId(bookingResponse.getId());
                        booking.setUserId(bookingResponse.getEvOwnerNIC());
                        booking.setStationName(bookingResponse.getChargingStationName());
                        booking.setStationId(selectedStationId);
                        booking.setStationAddress(selectedStationAddress);
                        booking.setReservationDate(selectedDate.getTime());
                        booking.setReservationTime(selectedDate.getTime());
                        booking.setDuration(bookingResponse.getDurationMinutes());
                        booking.setTotalCost(bookingResponse.getTotalAmount());
                        booking.setStatusFromString(bookingResponse.getStatus());
                        booking.setQrCode(bookingResponse.getQrCode());

                        // Pass booking to summary activity
                        Intent intent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
                        intent.putExtra("booking", booking);
                        startActivity(intent);
                        
                        // Reset button state
                        btnContinue.setEnabled(true);
                        btnContinue.setText("Continue");
                        
                        finish(); // Close this activity
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBookingActivity.this, 
                            "Failed to create booking: " + error, 
                            Toast.LENGTH_LONG).show();
                        
                        // Reset button state
                        btnContinue.setEnabled(true);
                        btnContinue.setText("Continue");
                    });
                }
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        android.util.Log.d("CreateBookingActivity", "onActivityResult - requestCode: " + requestCode + 
                          ", resultCode: " + resultCode + ", data: " + (data != null ? "present" : "null"));
        
        if (requestCode == STATION_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get selected station data
            selectedStationId = data.getStringExtra("selected_station_id");
            selectedStationName = data.getStringExtra("selected_station_name");
            selectedStationAddress = data.getStringExtra("selected_station_address");
            selectedStationPrice = data.getDoubleExtra("selected_station_price", 0.0);
            selectedStationAvailableSlots = data.getIntExtra("selected_station_available_slots", 0);
            
            android.util.Log.d("CreateBookingActivity", "Station selected: " + selectedStationName + 
                              " (ID: " + selectedStationId + ")");
            
            // Update UI with selected station
            updateSelectedStationUI();
            
            Toast.makeText(this, "Station selected: " + selectedStationName, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedStationUI() {
        if (selectedStationName != null) {
            android.util.Log.d("CreateBookingActivity", "Updating UI with selected station: " + selectedStationName);
            
            tvSelectedStation.setText(selectedStationName);
            tvStationAddress.setText(selectedStationAddress != null ? selectedStationAddress : "No address available");
            tvChargingRate.setText(String.format("Rs. %.2f / hour", selectedStationPrice));
            
            // Make sure the views are visible
            tvSelectedStation.setVisibility(View.VISIBLE);
            tvStationAddress.setVisibility(View.VISIBLE);
            tvChargingRate.setVisibility(View.VISIBLE);
            
            // Update cost calculation with new station price
            updateCostCalculation();
            
            // Enable continue button if station is selected
            updateContinueButton();
        }
    }

    private void updateCostCalculation() {
        if (selectedStationPrice > 0 && !etDuration.getText().toString().isEmpty()) {
            try {
                // Extract duration from string (e.g., "90 minutes" -> 90)
                String durationStr = etDuration.getText().toString();
                int durationMinutes = Integer.parseInt(durationStr.split(" ")[0]);
                
                // Calculate cost: (duration in hours) * price per hour
                double durationHours = durationMinutes / 60.0;
                double estimatedCost = durationHours * selectedStationPrice;
                
                // Update UI
                tvEstimatedCost.setText(String.format("Rs. %.2f", estimatedCost));
                tvEstimatedCost.setVisibility(View.VISIBLE);
                
                android.util.Log.d("CreateBookingActivity", 
                    "Cost calculated: " + durationMinutes + " minutes at Rs." + selectedStationPrice + "/hr = Rs." + estimatedCost);
            } catch (Exception e) {
                android.util.Log.e("CreateBookingActivity", "Error calculating cost: " + e.getMessage());
                tvEstimatedCost.setText("Cost calculation error");
            }
        } else {
            tvEstimatedCost.setText("Select station and duration");
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
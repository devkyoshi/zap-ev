package com.ead.zap.ui.owner.modals;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ead.zap.R;
import com.ead.zap.models.Booking;
import com.ead.zap.services.BookingService;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModifyReservationActivity extends AppCompatActivity {

    private TextInputEditText etBookingDate, etBookingTime, etDuration;
    private TextView tvStationName, tvStationAddress, tvOriginalDate, tvOriginalTime, 
                    tvOriginalDuration, tvChargingRate, tvEstimatedCost;
    private Button btnCancel, btnSaveChanges;
    private Booking booking;
    private BookingService bookingService;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_reservation);

        bookingService = new BookingService(this);
        
        setupToolbar();
        initViews();
        loadBookingData();
        setupDateAndTimePickers();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Modify Reservation");
        }
    }

    private void initViews() {
        etBookingDate = findViewById(R.id.et_booking_date);
        etBookingTime = findViewById(R.id.et_booking_time);
        etDuration = findViewById(R.id.et_duration);
        tvStationName = findViewById(R.id.tv_station_name);
        tvStationAddress = findViewById(R.id.tv_station_address);
        tvOriginalDate = findViewById(R.id.tv_original_date);
        tvOriginalTime = findViewById(R.id.tv_original_time);
        tvOriginalDuration = findViewById(R.id.tv_original_duration);
        tvChargingRate = findViewById(R.id.tv_charging_rate);
        tvEstimatedCost = findViewById(R.id.tv_estimated_cost);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
    }

    private void loadBookingData() {
        booking = (Booking) getIntent().getSerializableExtra("booking");
        
        if (booking == null) {
            Toast.makeText(this, "Error loading booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if booking can be modified (at least 12 hours before)
        Calendar bookingTime = Calendar.getInstance();
        bookingTime.setTime(booking.getReservationTime());
        
        Calendar minModificationTime = Calendar.getInstance();
        minModificationTime.add(Calendar.HOUR, 12);

        if (bookingTime.before(minModificationTime)) {
            Toast.makeText(this, "Cannot modify booking less than 12 hours before reservation time", 
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Populate station information
        tvStationName.setText(booking.getStationName());
        tvStationAddress.setText(booking.getStationAddress());

        // Show original booking details
        tvOriginalDate.setText(dateFormat.format(booking.getReservationDate()));
        tvOriginalTime.setText(timeFormat.format(booking.getReservationTime()));
        tvOriginalDuration.setText(String.format(Locale.getDefault(), "%d minutes", booking.getDuration()));

        // Pre-populate modification fields with current values
        selectedDate.setTime(booking.getReservationTime());
        etBookingDate.setText(dateFormat.format(booking.getReservationDate()));
        etBookingTime.setText(timeFormat.format(booking.getReservationTime()));
        etDuration.setText(String.valueOf(booking.getDuration()));

        // Show charging rate
        double ratePerMinute = booking.getTotalCost() / booking.getDuration();
        tvChargingRate.setText(String.format(Locale.getDefault(), "LKR %.2f per minute", ratePerMinute));
        
        updateEstimatedCost();
    }

    private void setupDateAndTimePickers() {
        // Date Picker
        etBookingDate.setOnClickListener(v -> showDatePicker());

        // Time Picker
        etBookingTime.setOnClickListener(v -> showTimePicker());

        // Duration change listener
        etDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateEstimatedCost();
            }
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
                    validateModificationDateTime();
                    updateEstimatedCost();
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
                    validateModificationDateTime();
                    updateEstimatedCost();
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void validateModificationDateTime() {
        Calendar now = Calendar.getInstance();
        Calendar minAllowedTime = Calendar.getInstance();
        minAllowedTime.add(Calendar.HOUR, 12); // Must modify at least 12 hours in advance

        if (selectedDate.before(minAllowedTime)) {
            etBookingTime.setError("Must book at least 12 hours in advance");
            btnSaveChanges.setEnabled(false);
        } else {
            etBookingTime.setError(null);
            btnSaveChanges.setEnabled(true);
        }
    }

    private void updateEstimatedCost() {
        String durationStr = etDuration.getText().toString().trim();
        if (!durationStr.isEmpty()) {
            try {
                int duration = Integer.parseInt(durationStr);
                double ratePerMinute = booking.getTotalCost() / booking.getDuration();
                double newCost = duration * ratePerMinute;
                tvEstimatedCost.setText(String.format(Locale.getDefault(), "LKR %.2f", newCost));
            } catch (NumberFormatException e) {
                tvEstimatedCost.setText("Invalid duration");
            }
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnSaveChanges.setOnClickListener(v -> saveModifiedBooking());
    }

    private void saveModifiedBooking() {
        if (!validateForm()) {
            return;
        }

        // Disable button to prevent multiple clicks
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Saving...");

        String durationStr = etDuration.getText().toString().trim();
        int newDuration = Integer.parseInt(durationStr);

        // Call the booking service to update the booking
        bookingService.updateBooking(
            booking.getBookingId(),
            selectedDate.getTime(),
            newDuration,
            "", // No notes for now
            new BookingService.BookingCallback() {
                @Override
                public void onSuccess(com.ead.zap.api.services.BookingApiService.BookingResponseDTO bookingResponse) {
                    runOnUiThread(() -> {
                        // Update local booking object with new data
                        booking.setReservationDate(selectedDate.getTime());
                        booking.setReservationTime(selectedDate.getTime());
                        booking.setDuration(bookingResponse.getDurationMinutes());
                        booking.setTotalCost(bookingResponse.getTotalAmount());
                        booking.setStatusFromString(bookingResponse.getStatus());
                        booking.setUpdatedAt(new Date());

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("modified_booking", booking);
                        setResult(RESULT_OK, resultIntent);
                        
                        Toast.makeText(ModifyReservationActivity.this, 
                            "Booking updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ModifyReservationActivity.this, 
                            "Failed to update booking: " + error, Toast.LENGTH_LONG).show();
                        
                        // Re-enable button
                        btnSaveChanges.setEnabled(true);
                        btnSaveChanges.setText("Save Changes");
                    });
                }
            }
        );
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (etBookingDate.getText().toString().isEmpty()) {
            etBookingDate.setError("Please select a date");
            isValid = false;
        }

        if (etBookingTime.getText().toString().isEmpty()) {
            etBookingTime.setError("Please select a time");
            isValid = false;
        }

        String durationStr = etDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            etDuration.setError("Please enter duration");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0 || duration > 480) { // Max 8 hours
                    etDuration.setError("Duration must be between 1 and 480 minutes");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etDuration.setError("Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
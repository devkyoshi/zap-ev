package com.ead.zap.ui.owner.modals;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ead.zap.R;
import com.ead.zap.models.Booking;
import com.ead.zap.ui.owner.EVOwnerMain;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private TextView tvBookingId, tvStationName, tvBookingDate, tvBookingTime, 
                    tvDuration, tvStatus, tvInstructions;
    private Button btnSaveQR, btnBackToHome, btnViewBookings;
    private Booking booking;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        setupToolbar();
        initViews();
        loadBookingData();
        generateQRCode();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Your QR Code");
        }
    }

    private void initViews() {
        ivQRCode = findViewById(R.id.iv_qr_code);
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvStationName = findViewById(R.id.tv_station_name);
        tvBookingDate = findViewById(R.id.tv_booking_date);
        tvBookingTime = findViewById(R.id.tv_booking_time);
        tvDuration = findViewById(R.id.tv_duration);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnSaveQR = findViewById(R.id.btn_save_qr);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
        btnViewBookings = findViewById(R.id.btn_view_bookings);
    }

    private void loadBookingData() {
        booking = (Booking) getIntent().getSerializableExtra("booking");
        
        if (booking == null) {
            Toast.makeText(this, "Error loading booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate booking information
        tvBookingId.setText(booking.getBookingId() != null ? booking.getBookingId() : "N/A");
        tvStationName.setText(booking.getStationName());
        tvBookingDate.setText(dateFormat.format(booking.getReservationDate()));
        tvBookingTime.setText(timeFormat.format(booking.getReservationTime()));
        tvDuration.setText(String.format(Locale.getDefault(), "%d minutes", booking.getDuration()));
        tvStatus.setText(booking.getStatus());

        // Set status color
        switch (booking.getStatus()) {
            case "APPROVED":
                tvStatus.setTextColor(getColor(R.color.primary_light));
                tvInstructions.setText("Great! Your booking has been approved. Show this QR code to the station operator when you arrive for charging.");
                break;
            case "PENDING":
                tvStatus.setTextColor(getColor(R.color.amber_600));
                tvInstructions.setText("Your booking is pending approval. You will receive a notification once it's approved. This QR code will be activated after approval.");
                break;
            default:
                tvStatus.setTextColor(getColor(R.color.black));
                tvInstructions.setText("Please wait for booking confirmation to use this QR code.");
                break;
        }
    }

    private void generateQRCode() {
        try {
            // Create QR code data - in a real app, this would be more secure
            String qrData = createQRCodeData();
            
            // Update booking with QR code data
            booking.setQrCode(qrData);
            
            // Generate QR code bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
            
            // Display QR code
            ivQRCode.setImageBitmap(bitmap);
            
        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String createQRCodeData() {
        // Create JSON-like string for QR code data
        // In production, this should be properly encrypted and secured
        return "{\n" +
                "  \"bookingId\": \"" + booking.getBookingId() + "\",\n" +
                "  \"userId\": \"" + booking.getUserId() + "\",\n" +
                "  \"stationId\": \"" + booking.getStationId() + "\",\n" +
                "  \"date\": \"" + dateFormat.format(booking.getReservationDate()) + "\",\n" +
                "  \"time\": \"" + timeFormat.format(booking.getReservationTime()) + "\",\n" +
                "  \"duration\": " + booking.getDuration() + ",\n" +
                "  \"status\": \"" + booking.getStatus() + "\",\n" +
                "  \"timestamp\": " + System.currentTimeMillis() + "\n" +
                "}";
    }

    private void setupClickListeners() {
        btnSaveQR.setOnClickListener(v -> {
            // In a real app, this would save the QR code to gallery
            // For now, we'll just show a toast message
            Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show();
        });

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, EVOwnerMain.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, EVOwnerMain.class);
            intent.putExtra("open_bookings_tab", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        // Navigate back to home instead of previous activity
        Intent intent = new Intent(this, EVOwnerMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
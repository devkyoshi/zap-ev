package com.ead.zap.ui.owner.modals;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ead.zap.R;
import com.google.android.material.card.MaterialCardView;
import com.ead.zap.models.Booking;
import com.ead.zap.models.BookingStatus;
import com.ead.zap.ui.owner.EVOwnerMain;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private TextView tvBookingId, tvStationName, tvBookingDate, tvBookingTime, 
                    tvDuration, tvStatus, tvInstructions;
    private Button btnSaveQR, btnBackToHome, btnViewBookings;
    private Booking booking;
    private Bitmap qrCodeBitmap;
    
    // UI Cards for different states
    private MaterialCardView cardQRCode, cardPendingStatus;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    // Permission launcher for storage permissions
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveQRCodeToGallery();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot save QR code to gallery.", Toast.LENGTH_SHORT).show();
                }
            });

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
        
        // Card views for different states
        cardQRCode = findViewById(R.id.card_qr_code);
        cardPendingStatus = findViewById(R.id.card_pending_status);
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
        tvStatus.setText(booking.getStatusString());

        // Set status color and UI visibility based on booking status
        switch (booking.getStatus()) {
            case APPROVED:
                tvStatus.setTextColor(getColor(R.color.primary_light));
                tvInstructions.setText("Great! Your booking has been approved. Show this QR code to the station operator when you arrive for charging.");
                
                // Show QR code, hide pending message
                cardQRCode.setVisibility(android.view.View.VISIBLE);
                cardPendingStatus.setVisibility(android.view.View.GONE);
                btnSaveQR.setVisibility(android.view.View.VISIBLE);
                btnSaveQR.setEnabled(true);
                break;
                
            case PENDING:
                tvStatus.setTextColor(getColor(R.color.amber_600));
                tvInstructions.setText("Your booking is pending approval. You will receive a notification once it's approved.");
                
                // Hide QR code, show pending message
                cardQRCode.setVisibility(android.view.View.GONE);
                cardPendingStatus.setVisibility(android.view.View.VISIBLE);
                btnSaveQR.setVisibility(android.view.View.GONE);
                btnSaveQR.setEnabled(false);
                break;
                
            default:
                tvStatus.setTextColor(getColor(R.color.black));
                tvInstructions.setText("Please wait for booking confirmation.");
                
                // Hide QR code, show pending message for other statuses
                cardQRCode.setVisibility(android.view.View.GONE);
                cardPendingStatus.setVisibility(android.view.View.VISIBLE);
                btnSaveQR.setVisibility(android.view.View.GONE);
                btnSaveQR.setEnabled(false);
                break;
        }
    }

    private void generateQRCode() {
        // Only generate QR code for approved bookings
        if (booking.getStatus() == BookingStatus.APPROVED && 
            booking.getQrCode() != null && !booking.getQrCode().isEmpty()) {
            
            try {
                // Use backend-generated QR code (properly formatted with hash)
                String qrData = booking.getQrCode();
                
                // Generate QR code bitmap
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrCodeBitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                
                // Display QR code
                ivQRCode.setImageBitmap(qrCodeBitmap);
                
            } catch (WriterException e) {
                Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            // For non-approved bookings, clear any existing QR code
            qrCodeBitmap = null;
            ivQRCode.setImageBitmap(null);
        }
    }

    private void checkPermissionAndSaveQR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                saveQRCodeToGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6+ uses WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                saveQRCodeToGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // Below Android 6, permissions are granted at install time
            saveQRCodeToGallery();
        }
    }

    private void saveQRCodeToGallery() {
        try {
            String fileName = "ZapEV_QR_" + booking.getBookingId() + "_" + System.currentTimeMillis();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ uses MediaStore API
                saveImageUsingMediaStore(fileName);
            } else {
                // Below Android 10 uses traditional file storage
                saveImageToExternalStorage(fileName);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveImageUsingMediaStore(String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ZapEV");

        ContentResolver resolver = getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                if (outputStream != null) {
                    qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(this, "QR code saved to Pictures/ZapEV folder", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            throw new IOException("Failed to create image file");
        }
    }

    private void saveImageToExternalStorage(String fileName) throws IOException {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File zapEvDir = new File(picturesDir, "ZapEV");
        
        if (!zapEvDir.exists() && !zapEvDir.mkdirs()) {
            throw new IOException("Failed to create ZapEV directory");
        }

        File imageFile = new File(zapEvDir, fileName + ".png");
        
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            
            // Notify the media scanner about the new image
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            
            Toast.makeText(this, "QR code saved to Pictures/ZapEV folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnSaveQR.setOnClickListener(v -> {
            if (qrCodeBitmap != null) {
                checkPermissionAndSaveQR();
            } else {
                Toast.makeText(this, "QR code not available", Toast.LENGTH_SHORT).show();
            }
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
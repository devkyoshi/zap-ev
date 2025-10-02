package com.ead.zap.ui.operator.modals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ead.zap.R;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = false;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !isScanning) {
                isScanning = true;
                handleQRResult(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Optional: handle result points for UI feedback
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        
        // Configure scanner for QR codes only
        Collection<com.google.zxing.BarcodeFormat> formats = Arrays.asList(com.google.zxing.BarcodeFormat.QR_CODE);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);

        // Set status text
        barcodeView.setStatusText("Scan a customer's QR code");
    }

    private void handleQRResult(String qrData) {
        try {
            // Parse JSON QR code data from EV Owner app
            // Expected JSON format with bookingId, userId, stationId, date, time, etc.
            if (qrData.contains("bookingId") && qrData.contains("stationId")) {
                // Simple JSON parsing (in production, use a proper JSON library)
                String bookingId = extractJsonValue(qrData, "bookingId");
                String userId = extractJsonValue(qrData, "userId");
                String stationId = extractJsonValue(qrData, "stationId");
                String date = extractJsonValue(qrData, "date");
                String time = extractJsonValue(qrData, "time");
                String duration = extractJsonValue(qrData, "duration");
                String status = extractJsonValue(qrData, "status");
                String customerName = extractJsonValue(qrData, "customerName");
                String stationName = extractJsonValue(qrData, "stationName");

                // Validate required fields
                if (bookingId != null && stationId != null && status != null) {
                    if ("APPROVED".equals(status)) {
                        // Navigate to booking verification with scanned data
                        Intent intent = new Intent(QRScannerActivity.this, BookingVerificationActivity.class);
                        intent.putExtra("booking_id", bookingId);
                        intent.putExtra("customer_name", customerName != null ? customerName : (userId != null ? userId : "Customer"));
                        intent.putExtra("station_id", stationId);
                        intent.putExtra("slot_number", "A1"); // Default slot for demo
                        intent.putExtra("start_time", time != null ? time : "N/A");
                        intent.putExtra("end_time", calculateEndTime(time, duration));
                        intent.putExtra("duration", duration);
                        intent.putExtra("date", date);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "This booking is not approved yet. Status: " + status, Toast.LENGTH_LONG).show();
                        isScanning = false;
                    }
                } else {
                    Toast.makeText(this, "Invalid booking QR code - missing required information", Toast.LENGTH_LONG).show();
                    isScanning = false;
                }
            } else {
                Toast.makeText(this, "This is not a valid booking QR code", Toast.LENGTH_LONG).show();
                isScanning = false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error reading QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
            isScanning = false;
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            // Simple JSON value extraction (not production-ready)
            String searchKey = "\"" + key + "\": \"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
            
            // Try numeric values without quotes
            searchKey = "\"" + key + "\": ";
            startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = json.indexOf("\n", startIndex);
                if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String calculateEndTime(String startTime, String duration) {
        if (startTime == null || duration == null) return "N/A";
        
        try {
            // Simple time calculation (not production-ready)
            // This is a basic implementation - in production use proper date/time libraries
            int durationMinutes = Integer.parseInt(duration);
            
            // For demo purposes, just add duration to display
            return startTime + " (+" + durationMinutes + " min)";
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
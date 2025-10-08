package com.ead.zap.ui.operator.modals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ead.zap.R;
import com.ead.zap.services.OperatorService;
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
    private OperatorService operatorService;

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

        // Initialize services
        operatorService = new OperatorService(this);

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
        // Show loading while verifying QR code
        barcodeView.setStatusText("Verifying QR code...");
        
        // Use OperatorService to verify QR code with backend
        operatorService.verifyQRCode(qrData, new OperatorService.QRVerificationCallback() {
            @Override
            public void onSuccess(OperatorService.BookingVerificationResult result) {
                runOnUiThread(() -> {
                    if (result.isValid()) {
                        // Navigate to booking verification with verified data
                        Intent intent = new Intent(QRScannerActivity.this, BookingVerificationActivity.class);
                        intent.putExtra("booking_id", result.getBookingId());
                        intent.putExtra("customer_name", result.getCustomerName());
                        intent.putExtra("station_id", result.getStationId());
                        intent.putExtra("slot_number", result.getSlotNumber());
                        intent.putExtra("start_time", result.getStartTime());
                        intent.putExtra("end_time", result.getEndTime());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(QRScannerActivity.this, 
                            "QR Verification Failed: " + result.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        resetScanning();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(QRScannerActivity.this, 
                        "Error verifying QR code: " + error, 
                        Toast.LENGTH_LONG).show();
                    resetScanning();
                });
            }
        });
    }

    private void resetScanning() {
        isScanning = false;
        barcodeView.setStatusText("Scan a customer's QR code");
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
package com.ead.zap.ui.operator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;

public class OperatorQRScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private Button btnScanQR;
    private TextView tvWelcome, tvInstructions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operator_qr_scanner, container, false);
        
        initViews(view);
        setupClickListeners();
        
        return view;
    }

    private void initViews(View view) {
        btnScanQR = view.findViewById(R.id.btn_scan_qr);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvInstructions = view.findViewById(R.id.tv_instructions);
    }

    private void setupClickListeners() {
        btnScanQR.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openQRScanner();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }

    private void openQRScanner() {
        // Open the real QR scanner activity
        Intent intent = new Intent(getActivity(), com.ead.zap.ui.operator.modals.QRScannerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openQRScanner();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
            }
        }
    }
}
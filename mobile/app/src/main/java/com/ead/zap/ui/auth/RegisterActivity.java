package com.ead.zap.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import com.ead.zap.R;
import com.ead.zap.models.EVOwner;
import com.ead.zap.models.auth.EVOwnerRegistrationRequest;
import com.ead.zap.services.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etNic, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin, tvEvOwner, tvStationOperator;
    private CardView cardEvOwner, cardStationOperator;
    private NestedScrollView scrollView;
    private String selectedRole = "EV_OWNER"; // Default role
    
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Adjust layout when keyboard appears
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        authService = new AuthService(this);
        
        initViews();
        setupClickListeners();
        setupRoleSelection();
        setupFocusListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etNic = findViewById(R.id.et_nic);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        cardEvOwner = findViewById(R.id.card_ev_owner);
        cardStationOperator = findViewById(R.id.card_station_operator);
        scrollView = findViewById(R.id.nestedScrollView);
        tvEvOwner = findViewById(R.id.tv_ev_owner);
        tvStationOperator = findViewById(R.id.tv_station_operator);
    }

    private void setupFocusListeners() {
        // Set up focus change listeners to scroll to focused field
        View[] fields = {etFirstName, etLastName, etNic, etEmail, etPhone, etPassword, etConfirmPassword};

        for (View field : fields) {
            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Scroll to the focused field with a slight delay
                    scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom()), 100);
                }
            });
        }
    }

    private void setupRoleSelection() {
        // Set initial selection - only EV_OWNER available on mobile
        selectRole("EV_OWNER");

        cardEvOwner.setOnClickListener(v -> {
            selectRole("EV_OWNER");
            hideKeyboard();
        });

        // Disable station operator registration in mobile app
        cardStationOperator.setOnClickListener(v -> {
            hideKeyboard();
            Toast.makeText(this, "Station Operator accounts are created by administrators via web interface. Please contact support.", Toast.LENGTH_LONG).show();
        });
        
        // Make station operator card visually disabled
        cardStationOperator.setEnabled(false);
        cardStationOperator.setAlpha(0.5f);
    }

    private void selectRole(String role) {
        // Only allow EV_OWNER role on mobile app
        if (!role.equals("EV_OWNER")) {
            return;
        }
        
        selectedRole = role;

        // Always select EV_OWNER (only available role on mobile)
        cardEvOwner.setCardBackgroundColor(getColor(R.color.accent));
        cardEvOwner.setCardElevation(4f);
        tvEvOwner.setTextColor(getColor(R.color.white));
        
        // Keep station operator card visually disabled
        tvStationOperator.setTextColor(getColor(R.color.accent));
        cardStationOperator.setCardBackgroundColor(getColor(R.color.white));
        cardStationOperator.setCardElevation(2f);
        cardStationOperator.setAlpha(0.5f);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            hideKeyboard();
            performRegistration();
        });

        tvLogin.setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        // Hide keyboard when clicking outside fields
        scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void performRegistration() {
        String firstName = Objects.requireNonNull(etFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(etLastName.getText()).toString().trim();
        String nic = Objects.requireNonNull(etNic.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        // Validate inputs
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            scrollView.smoothScrollTo(0, etFirstName.getTop());
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            scrollView.smoothScrollTo(0, etLastName.getTop());
            return;
        }

        if (nic.isEmpty()) {
            etNic.setError("NIC is required");
            scrollView.smoothScrollTo(0, etNic.getTop());
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            scrollView.smoothScrollTo(0, etEmail.getTop());
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            scrollView.smoothScrollTo(0, etPhone.getTop());
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            scrollView.smoothScrollTo(0, etPassword.getTop());
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            scrollView.smoothScrollTo(0, etConfirmPassword.getTop());
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            scrollView.smoothScrollTo(0, etConfirmPassword.getTop());
            return;
        }

        // Only EV Owner registration is available in mobile app
        // Station Operators are created by BackOffice users via web interface
        if (!selectedRole.equals("EV_OWNER")) {
            Toast.makeText(this, "Station Operator accounts are created by administrators via web interface. Please contact support.", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable register button to prevent multiple requests
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        // Create registration request
        EVOwnerRegistrationRequest request = new EVOwnerRegistrationRequest();
        request.setNic(nic);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPhoneNumber(phone);
        request.setPassword(password);
        
        // For now, create empty vehicle list - user can add vehicles later
        request.setVehicleDetails(new ArrayList<>());

        // Log the registration request for debugging
        Log.d("RegisterActivity", "Registration request data:");
        Log.d("RegisterActivity", "NIC: " + request.getNic());
        Log.d("RegisterActivity", "FirstName: " + request.getFirstName());
        Log.d("RegisterActivity", "LastName: " + request.getLastName());
        Log.d("RegisterActivity", "Email: " + request.getEmail());
        Log.d("RegisterActivity", "PhoneNumber: " + request.getPhoneNumber());
        Log.d("RegisterActivity", "Password length: " + (request.getPassword() != null ? request.getPassword().length() : 0));

        // Perform registration
        authService.registerEVOwner(request, new AuthService.RegistrationCallback() {
            @Override
            public void onSuccess(EVOwner evOwner) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please login to continue.", Toast.LENGTH_LONG).show();
                    
                    // Navigate to login screen
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("registered_nic", nic); // Pre-fill NIC in login
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    resetRegisterButton();
                });
            }
        });
    }

    /**
     * Reset register button to original state
     */
    private void resetRegisterButton() {
        btnRegister.setEnabled(true);
        btnRegister.setText("Register");
    }
    
    /**
     * Navigate to login activity after successful registration
     */
    private void navigateToLoginActivity(String nic) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("registered_nic", nic); // Pre-fill NIC in login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
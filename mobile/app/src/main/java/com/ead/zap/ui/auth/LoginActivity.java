package com.ead.zap.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import com.ead.zap.MainActivity;
import com.ead.zap.R;
import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.services.AuthService;
import com.ead.zap.ui.owner.EVOwnerMain;
import com.ead.zap.utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    
    private AuthService authService;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);
        preferenceManager = new PreferenceManager(this);
        
        // Check if user is already authenticated
        checkAuthenticationStatus();
        
        initViews();
        setupClickListeners();
        
        // Check if NIC was passed from registration
        String registeredNic = getIntent().getStringExtra("registered_nic");
        if (registeredNic != null && !registeredNic.isEmpty()) {
            etEmail.setText(registeredNic);
            etPassword.requestFocus();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        
        // Add input validation listeners
        setupInputValidation();
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvSignUp.setOnClickListener(v -> {
            // Navigate to SignUp activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Navigate to Forgot Password activity
            Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String emailOrNic = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Clear any previous error messages
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Validate inputs
        if (!validateInputs(emailOrNic, password)) {
            return;
        }

        // Show loading state
        showLoadingState(true);

        // Determine if input is NIC (for EV Owners) or username/email (for other users)
        if (isNIC(emailOrNic)) {
            // Login as EV Owner using NIC
            authService.loginEVOwner(emailOrNic, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthResponse response) {
                    runOnUiThread(() -> {
                        showLoadingState(false);
                        Toast.makeText(LoginActivity.this, "Welcome " + response.getUserType() + "!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity(response);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoadingState(false);
                        handleLoginError(error);
                    });
                }
            });
        } else {
            // Login as regular user (StationOperator)
            authService.login(emailOrNic, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthResponse response) {
                    runOnUiThread(() -> {
                        showLoadingState(false);
                        Toast.makeText(LoginActivity.this, "Welcome " + response.getUserType() + "!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity(response);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoadingState(false);
                        handleLoginError(error);
                    });
                }
            });
        }
    }
    
    /**
     * Check if user is already authenticated and navigate accordingly
     */
    private void checkAuthenticationStatus() {
        if (authService.isAuthenticated()) {
            if (authService.isTokenExpired()) {
                // Try to refresh token
                authService.refreshToken(new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        // Token refreshed, navigate to main activity
                        runOnUiThread(() -> navigateToMainActivity(response));
                    }

                    @Override
                    public void onError(String error) {
                        // Refresh failed, stay on login screen
                        // Clear invalid auth data
                        preferenceManager.clearAuthData();
                    }
                });
            } else {
                // Token is valid, create mock response and navigate
                AuthResponse mockResponse = new AuthResponse();
                mockResponse.setUserType(authService.getCurrentUserType());
                mockResponse.setUserId(authService.getCurrentUserId());
                navigateToMainActivity(mockResponse);
            }
        }
    }
    
    /**
     * Navigate to appropriate main activity based on user type
     */
    private void navigateToMainActivity(AuthResponse response) {
        Intent intent;
        
        if (response.isEVOwner()) {
            intent = new Intent(LoginActivity.this, EVOwnerMain.class);
        } else if (response.isStationOperator()) {
            intent = new Intent(LoginActivity.this, com.ead.zap.ui.operator.StationOperatorMain.class);
        } else {
            // BackOffice users should use web interface, not mobile app
            Toast.makeText(this, "BackOffice users should use the web application", Toast.LENGTH_LONG).show();
            resetLoginButton();
            return;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Check if input string is a NIC format
     * Supports both old format (9 digits + V/X) and new format (12 digits)
     */
    private boolean isNIC(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String trimmedInput = input.trim();
        
        // Check for old NIC format: 9 digits followed by V or X
        if (trimmedInput.length() == 10 && 
            (trimmedInput.toUpperCase().endsWith("V") || trimmedInput.toUpperCase().endsWith("X"))) {
            String digits = trimmedInput.substring(0, 9);
            return digits.matches("\\d{9}"); // Check if first 9 characters are digits
        }
        
        // Check for new NIC format: 12 digits only
        if (trimmedInput.length() == 12) {
            return trimmedInput.matches("\\d{12}"); // Check if all 12 characters are digits
        }
        
        return false;
    }
    
    /**
     * Setup input validation listeners
     */
    private void setupInputValidation() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Validate user inputs with specific error messages
     */
    private boolean validateInputs(String emailOrNic, String password) {
        boolean isValid = true;

        if (emailOrNic.isEmpty()) {
            emailInputLayout.setError("Please enter your NIC or username");
            isValid = false;
        } else if (emailOrNic.length() < 3) {
            emailInputLayout.setError("Input must be at least 3 characters long");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Please enter your password");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Show or hide loading state
     */
    private void showLoadingState(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Signing in...");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Handle login errors with appropriate user feedback
     */
    private void handleLoginError(String error) {
        // Check for specific error types and provide helpful messages
        if (error.toLowerCase().contains("invalid credentials") || 
            error.toLowerCase().contains("unauthorized") ||
            error.toLowerCase().contains("login failed")) {
            emailInputLayout.setError("Invalid NIC/username or password");
            Toast.makeText(this, "Please check your credentials and try again", Toast.LENGTH_LONG).show();
        } else if (error.toLowerCase().contains("network") || 
                   error.toLowerCase().contains("connection")) {
            Toast.makeText(this, "Network error. Please check your connection and try again", Toast.LENGTH_LONG).show();
        } else if (error.toLowerCase().contains("server") || 
                   error.toLowerCase().contains("timeout")) {
            Toast.makeText(this, "Server error. Please try again later", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Reset login button to original state (deprecated - use showLoadingState instead)
     */
    @Deprecated
    private void resetLoginButton() {
        showLoadingState(false);
    }
}